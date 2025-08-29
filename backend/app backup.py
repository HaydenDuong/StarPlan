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
# Make sure this file is in the same directory as app.py
TEMPLATE_PATH = "SEEK-resume-template-1.docx"

def save_to_word(tailored_resume_json, output_path="tailored_resume.docx"):
    """
    Saves a tailored resume to a Word document using a pre-existing docx template.
    It expects the tailored resume content as a JSON object from the OpenAI API.
    
    Args:
        tailored_resume_json (str): The JSON string from the OpenAI API containing
                                     the tailored resume data.
        output_path (str): The path to save the new Word document.
    """
    try:
        # Load the template
        doc = DocxTemplate(TEMPLATE_PATH)

        # Load the JSON data. This data will be used to fill the placeholders
        # in the Word template (e.g., {{ name }}).
        context = json.loads(tailored_resume_json)

        # Render the document with the context data
        doc.render(context)
        
        # Save the new document with the filled-in content
        doc.save(output_path)
        return output_path

    except FileNotFoundError:
        # If the template file is not found, return an error
        return jsonify({"error": f"Template file '{TEMPLATE_PATH}' not found. Please ensure it is in the same directory."}), 400
    except json.JSONDecodeError:
        # If the OpenAI response is not valid JSON, return an error
        return jsonify({"error": "OpenAI response was not valid JSON."}), 500
    except Exception as e:
        # General error handling
        return jsonify({"error": f"An error occurred while creating the Word document: {e}"}), 500

# Endpoint for uploading a resume file (PDF)
@app.route("/upload", methods=["POST"])
def upload_resume():
    """
    Handles resume tailoring from a PDF file upload.
    It reads the PDF, creates a prompt for OpenAI, and returns a tailored Word document.
    """
    job_desc = request.form.get("job_description")
    file = request.files.get("resume")

    resume_text = ""
    if file and file.filename.endswith(".pdf"):
        try:
            with pdfplumber.open(file) as pdf:
                resume_text = " ".join([page.extract_text() for page in pdf.pages if page.extract_text()])
        except Exception as e:
            return jsonify({"error": f"Error processing PDF file: {e}"}), 400

    if not resume_text:
        return jsonify({"error": "No resume text extracted. Please check the file format."}), 400

    try:
        # We instruct the model to return a highly structured JSON object
        response = client.chat.completions.create(
            model="gpt-5",
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
        tailored_resume_json = response.choices[0].message.content
    except Exception as e:
        return jsonify({"error": f"Error calling OpenAI API: {e}"}), 500

    try:
        output_path = save_to_word(tailored_resume_json)
        return send_file(output_path, as_attachment=True)
    except Exception as e:
        return jsonify({"error": f"Error creating or sending Word file: {e}"}), 500


# Endpoint to handle resume data submitted via JSON
@app.route("/submit_data", methods=["POST"])
def submit_data():
    """
    Handles resume tailoring from a JSON data submission.
    """
    try:
        data = request.get_json()
        job_desc = data.get("job_description")
        
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

        # Call OpenAI API to generate the structured resume
        response = client.chat.completions.create(
            model="gpt-5",
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
        tailored_resume_json = response.choices[0].message.content

        # Save to Word file and return for download
        output_path = save_to_word(tailored_resume_json)
        return send_file(output_path, as_attachment=True)

    except Exception as e:
        return jsonify({"error": f"An error occurred: {e}"}), 500

if __name__ == "__main__":
    app.run(debug=True)