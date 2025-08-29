import os
import json
import pdfplumber
from flask import Flask, request, send_file, jsonify
from openai import OpenAI
from dotenv import load_dotenv
from docxtpl import DocxTemplate

# Load environment variables from .env file
load_dotenv()

# Initialize Flask app and OpenAI client
app = Flask(__name__)
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# Define the template file you want to use
TEMPLATE_PATH = "SEEK-resume-template-1.docx"

def save_to_word(context, output_path="tailored_resume.docx"):
    """
    Saves a tailored resume to a Word document using a pre-existing docx template.
    It expects the tailored resume content as a context dictionary from the OpenAI API.
    
    Args:
        context (dict): The dictionary containing the tailored resume data.
        output_path (str): The path to save the new Word document.
    """
    try:
        doc = DocxTemplate(TEMPLATE_PATH)
        doc.render(context)
        doc.save(output_path)
        return output_path
    except FileNotFoundError:
        return jsonify({"error": f"Template file '{TEMPLATE_PATH}' not found. Please ensure it is in the same directory."}), 400
    except Exception as e:
        return jsonify({"error": f"An error occurred while creating the Word document: {e}"}), 500

def get_job_description_by_id(job_id):
    """
    Loads job listings and returns the full job description for a given ID.
    """
    try:
        with open('job_listings.json', 'r') as f:
            jobs = json.load(f)
            for job in jobs:
                if job.get('id') == job_id:
                    return json.dumps(job) # Return as a string to pass to the API
    except (FileNotFoundError, json.JSONDecodeError) as e:
        return jsonify({"error": f"Error loading job listings: {e}"}), 500
    return None

# New Endpoint for general resume generation (no job description)
@app.route("/generate_resume", methods=["POST"])
def generate_resume():
    """
    Generates a general resume without tailoring to a specific job.
    """
    file = request.files.get("resume")
    resume_text = ""
    if file and file.filename.endswith(".pdf"):
        with pdfplumber.open(file) as pdf:
            resume_text = " ".join([page.extract_text() for page in pdf.pages if page.extract_text()])
    else:
        # Handle JSON data as an alternative input
        data = request.get_json()
        if data:
            resume_text = json.dumps(data)
    
    if not resume_text:
        return jsonify({"error": "No resume text extracted or provided."}), 400

    try:
        response = client.chat.completions.create(
            model="gpt-5",
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": """You are a professional career coach. Your task is to generate a general resume based on the provided data. The output MUST be a JSON object with the following keys:
                - 'name': The person's full name.
                - 'contact_info': A single string with email, phone, and address separated by a pipe (|).
                - 'summary': A concise professional summary.
                - 'experience': An array of job objects. Each object MUST have 'title', 'company', 'dates', and 'details' (an array of strings for bullet points).
                - 'education': An array of education objects. Each object MUST have 'degree', 'university', and 'dates'.
                - 'skills': A single string of comma-separated skills.
                - 'languages': A single string of comma-separated languages.
                The output MUST be valid JSON.
                """},
                {"role": "user", "content": f"Please generate a general resume from this information:\n{resume_text}\n\nThe output MUST be valid JSON."}
            ]
        )
        context = json.loads(response.choices[0].message.content)
        output_path = save_to_word(context)
        return send_file(output_path, as_attachment=True, download_name="general_resume.docx")

    except Exception as e:
        return jsonify({"error": f"An error occurred: {e}"}), 500

# New Endpoint to get resume feedback
@app.route("/get_feedback", methods=["POST"])
def get_feedback():
    """
    Analyzes a resume against a job description and provides feedback.
    """
    job_id = request.form.get("job_id")
    file = request.files.get("resume")
    
    if not job_id or not file:
        return jsonify({"error": "Missing job_id or resume file."}), 400

    job_desc = get_job_description_by_id(int(job_id))
    if not job_desc:
        return jsonify({"error": "Job not found."}), 404

    resume_text = ""
    with pdfplumber.open(file) as pdf:
        resume_text = " ".join([page.extract_text() for page in pdf.pages if page.extract_text()])

    try:
        response = client.chat.completions.create(
            model="gpt-4.1-mini",
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": """You are a professional career coach. Your task is to provide feedback on a resume based on a job description. The feedback MUST be a JSON object with the following keys and structure. Score each criterion on a scale of 0 to 10.
                {
                    "overall_score": { "score": 77, "justification": "Overall, the resume is well-written and aligns with the job description, but it could be improved by adding more quantifiable achievements." },
                    "impact_score": { "score": 77, "justification": "The resume uses strong action verbs, but lacks quantifiable metrics to show impact." },
                    "brevity_score": { "score": 75, "justification": "The resume is a good length, but some bullet points could be more concise." },
                    "style_score": { "score": 79, "justification": "The resume's style is generally good, but there are some inconsistencies in formatting." },
                    "feedback": {
                        "impact": {
                            "Quantifying Impact": { "score": 5, "justification": "The resume lacks specific numbers or metrics to quantify achievements." },
                            "Action Verb Use": { "score": 10, "justification": "The resume consistently uses strong, active voice verbs." },
                            "Accomplishments": { "score": 10, "justification": "The resume clearly highlights key accomplishments." },
                            "Repetition": { "score": 6, "justification": "There is some repetition of skills and responsibilities across different roles." }
                        },
                        "brevity": {
                            "Length": { "score": 7, "justification": "The resume is an appropriate length, fitting on one page." },
                            "Filler Words": { "score": 10, "justification": "The resume avoids unnecessary filler words and phrases." },
                            "Total Bullet Points": { "score": 7, "justification": "The number of bullet points per role is slightly high, which can make it hard to read." },
                            "Bullet Points Length": { "score": 8, "justification": "Most bullet points are concise, but a few are a bit long." }
                        },
                        "style": {
                            "Sections": { "score": 10, "justification": "The resume is well-organized with clear sections." },
                            "Personal Pronouns": { "score": 7, "justification": "The resume mostly avoids personal pronouns, but they are used in a few places." },
                            "Buzzwords & Cliches": { "score": 7, "justification": "A few buzzwords are present, which could be replaced with more specific language." },
                            "Active Voice": { "score": 10, "justification": "The resume is written almost entirely in the active voice." },
                            "Consistency": { "score": 0, "justification": "There are some inconsistencies in the formatting of dates and titles." },
                            "Date Order": { "score": 10, "justification": "Dates are in a consistent reverse-chronological order." }
                        },
                        "other": {
                            "Spell Check": { "score": 10, "justification": "No spelling or grammatical errors were found." },
                            "Target My CV": { "score": 10, "justification": "The resume is well-tailored to the specific job description." },
                            "Rate CV Review": { "score": 10, "justification": "The overall quality of the resume is excellent for this job application." }
                        }
                    }
                }
                The 'feedback' key should contain a nested JSON object with scores and justifications for each sub-criterion. The 'overall_score', 'impact_score', 'brevity_score', and 'style_score' should be derived from the sub-scores with overall justifications. The output MUST be valid JSON.
                """ },
                {"role": "user", "content": f"Resume:\n{resume_text}\n\nJob Description:\n{job_desc}\n\nProvide feedback as a JSON object with the specified structure. The output MUST be valid JSON."}
            ]
        )
        feedback_json = json.loads(response.choices[0].message.content)
        return jsonify({"feedback": feedback_json})
    except Exception as e:
        return jsonify({"error": f"An error occurred: {e}"}), 500

# Updated Endpoint for file upload tailoring
@app.route("/upload", methods=["POST"])
def upload_tailored_resume():
    """
    Handles resume tailoring from a PDF file upload using a job_id.
    """
    job_id = request.form.get("job_id")
    file = request.files.get("resume")

    if not job_id or not file:
        return jsonify({"error": "Missing job_id or resume file."}), 400

    job_desc = get_job_description_by_id(int(job_id))
    if not job_desc:
        return jsonify({"error": "Job not found."}), 404

    resume_text = ""
    with pdfplumber.open(file) as pdf:
        resume_text = " ".join([page.extract_text() for page in pdf.pages if page.extract_text()])

    try:
        response = client.chat.completions.create(
            model="gpt-4.1-mini",
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": """You are a professional career coach. Your task is to tailor a resume for a specific job description. The output MUST be a JSON object with the following keys. This is crucial for filling a resume template:
                - 'name': The person's full name.
                - 'contact_info': A single string with email, phone, and address separated by a pipe (|).
                - 'summary': A concise professional summary.
                - 'experience': An array of job objects. Each object MUST have 'title', 'company', 'dates', and 'details' (an array of strings for bullet points).
                - 'education': An array of education objects. Each object MUST have 'degree', 'university', and 'dates'.
                - 'skills': A single string of comma-separated skills.
                - 'languages': A single string of comma-separated languages.
                The output MUST be valid JSON.
                """},
                {"role": "user", "content": f"Resume:\n{resume_text}\n\nJob description:\n{job_desc}\n\nPlease tailor the resume to match the job. The output MUST be valid JSON."}
            ]
        )
        context = json.loads(response.choices[0].message.content)
        output_path = save_to_word(context)
        return send_file(output_path, as_attachment=True, download_name="tailored_resume.docx")
    except Exception as e:
        return jsonify({"error": f"An error occurred: {e}"}), 500

# Updated Endpoint for JSON data tailoring
@app.route("/submit_data", methods=["POST"])
def submit_data():
    """
    Handles resume tailoring from a JSON data submission using a job_id.
    """
    data = request.get_json()
    job_id = data.get("job_id")

    if not job_id:
        return jsonify({"error": "Missing job_id in JSON data."}), 400

    job_desc = get_job_description_by_id(int(job_id))
    if not job_desc:
        return jsonify({"error": "Job not found."}), 404

    # We'll reformat the input data into a single text block for the API
    personal_info = data.get("personal_info", {})
    objective = data.get("objective", {})
    experience = data.get("experience", [])
    education = data.get("education", [])
    skills = data.get("skills", [])
    languages = data.get("languages", [])

    resume_text_from_data = f"""
Personal Information:
Name: {personal_info.get('name', 'N/A')}
Email: {personal_info.get('email', 'N/A')}
Contact No: {personal_info.get('contact_no', 'N/A')}

Objective:
Industry: {objective.get('industry', 'N/A')}
Level of Work: {objective.get('level_of_work', 'N/A')}
Company Scale: {objective.get('company_scale', 'N/A')}

Experience:
"""
    for exp in experience:
        resume_text_from_data += f"- Company: {exp.get('company', 'N/A')}, From: {exp.get('from', 'N/A')}, To: {exp.get('to', 'N/A')}\n"
        resume_text_from_data += f"  Responsibilities: {exp.get('responsibilities', 'N/A')}\n"

    resume_text_from_data += f"""
Education:
"""
    for edu in education:
        resume_text_from_data += f"- {edu.get('degree', 'N/A')} at {edu.get('university', 'N/A')}\n"

    resume_text_from_data += f"""
Skills:
"""
    for skill in skills:
        resume_text_from_data += f"- {skill}\n"
    
    resume_text_from_data += f"""
Languages:
"""
    for lang in languages:
        resume_text_from_data += f"- {lang}\n"
    
    try:
        response = client.chat.completions.create(
            model="gpt-4.1-mini",
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": """You are a professional career coach. Your task is to tailor a resume for a specific job description. The output MUST be a JSON object with the following keys. This is crucial for filling a resume template:
                - 'name': The person's full name.
                - 'contact_info': A single string with email, phone, and address separated by a pipe (|).
                - 'summary': A concise professional summary.
                - 'experience': An array of job objects. Each object MUST have 'title', 'company', 'dates', and 'details' (an array of strings for bullet points).
                - 'education': An array of education objects. Each object MUST have 'degree', 'university', and 'dates'.
                - 'skills': A single string of comma-separated skills.
                - 'languages': A single string of comma-separated languages.
                The output MUST be valid JSON.
                """},
                {"role": "user", "content": f"Resume Data:\n{resume_text_from_data}\n\nJob description:\n{job_desc}\n\nPlease tailor the resume to match the job. The output MUST be valid JSON."}
            ]
        )
        context = json.loads(response.choices[0].message.content)
        output_path = save_to_word(context)
        return send_file(output_path, as_attachment=True, download_name="tailored_resume.docx")
    except Exception as e:
        return jsonify({"error": f"An error occurred: {e}"}), 500

if __name__ == "__main__":
    app.run(debug=True)