package com.example.starplan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResumeBuilderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resume_builder);

        initViews();
        setupClickListeners();
        setupDropdowns();
        setupSkillsInput();
        setupLanguageInput();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        // Expandable section click listeners
        findViewById(R.id.cardPersonalInfo).setOnClickListener(v -> 
            toggleSection(findViewById(R.id.contentPersonalInfo), findViewById(R.id.expandPersonalInfo)));
            
        findViewById(R.id.cardObjectives).setOnClickListener(v -> 
            toggleSection(findViewById(R.id.contentObjectives), findViewById(R.id.expandObjectives)));
            
        findViewById(R.id.cardExperience).setOnClickListener(v -> 
            toggleSection(findViewById(R.id.contentExperience), findViewById(R.id.expandExperience)));
            
        findViewById(R.id.cardEducation).setOnClickListener(v -> 
            toggleSection(findViewById(R.id.contentEducation), findViewById(R.id.expandEducation)));
            
        findViewById(R.id.cardSkills).setOnClickListener(v -> 
            toggleSection(findViewById(R.id.contentSkills), findViewById(R.id.expandSkills)));
            
        findViewById(R.id.cardLanguage).setOnClickListener(v -> 
            toggleSection(findViewById(R.id.contentLanguage), findViewById(R.id.expandLanguage)));
            
        // Current job checkbox functionality
        setupCurrentJobCheckbox();
            
        // Create Resume button
        findViewById(R.id.btnCreateResume).setOnClickListener(v -> createResume());
    }

    private void toggleSection(LinearLayout content, ImageView expandIcon) {
        if (content.getVisibility() == View.VISIBLE) {
            // Collapse
            content.setVisibility(View.GONE);
            expandIcon.setRotation(0f);
        } else {
            // Expand
            content.setVisibility(View.VISIBLE);
            expandIcon.setRotation(180f);
        }
    }

    private void setupCurrentJobCheckbox() {
        CheckBox currentJobCheckbox = findViewById(R.id.cbCurrentJobExp);
        EditText toDateField = findViewById(R.id.etToDateExp);

        if (currentJobCheckbox != null && toDateField != null) {
            currentJobCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    toDateField.setText("Present");
                    toDateField.setEnabled(false);
                } else {
                    toDateField.setText("");
                    toDateField.setEnabled(true);
                }
            });
        }
    }

    private void setupDropdowns() {
        // Industry dropdown
        String[] industries = {"Technology", "Healthcare", "Finance", "Education", "Marketing", "Engineering", "Design", "Sales", "Manufacturing", "Retail"};
        AutoCompleteTextView industryDropdown = findViewById(R.id.spinnerIndustryObj);
        if (industryDropdown != null) {
            ArrayAdapter<String> industryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, industries);
            industryDropdown.setAdapter(industryAdapter);
        }

        // Level of work dropdown
        String[] levels = {"Entry Level", "Mid Level", "Senior Level", "Executive Level"};
        AutoCompleteTextView levelDropdown = findViewById(R.id.spinnerLevelOfWorkObj);
        if (levelDropdown != null) {
            ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels);
            levelDropdown.setAdapter(levelAdapter);
        }

        // Company scale dropdown
        String[] scales = {"Startup (1-50)", "Small (51-200)", "Medium (201-1000)", "Large (1000+)"};
        AutoCompleteTextView scaleDropdown = findViewById(R.id.spinnerCompanyScaleObj);
        if (scaleDropdown != null) {
            ArrayAdapter<String> scaleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, scales);
            scaleDropdown.setAdapter(scaleAdapter);
        }
    }

    private void setupSkillsInput() {
        TextInputEditText skillInput = findViewById(R.id.etSkillInput);
        ChipGroup skillsChipGroup = findViewById(R.id.chipGroupSkills);
        ChipGroup suggestionsChipGroup = findViewById(R.id.chipGroupSuggestions);

        // Popular skills suggestions
        String[] popularSkills = {"JavaScript", "Python", "Java", "React", "Node.js", "SQL", "Leadership", "Communication", "Project Management", "Problem Solving", "Teamwork", "Data Analysis"};
        
        for (String skill : popularSkills) {
            Chip suggestionChip = new Chip(this);
            suggestionChip.setText(skill);
            suggestionChip.setClickable(true);
            suggestionChip.setCheckable(false);
            suggestionChip.setOnClickListener(v -> {
                addSkillChip(skill, skillsChipGroup);
                suggestionsChipGroup.removeView(suggestionChip);
            });
            suggestionsChipGroup.addView(suggestionChip);
        }

        // Handle Enter key on skill input
        if (skillInput != null) {
            skillInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String skillText = skillInput.getText().toString().trim();
                    if (!skillText.isEmpty()) {
                        addSkillChip(skillText, skillsChipGroup);
                        skillInput.setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void addSkillChip(String skillText, ChipGroup chipGroup) {
        // Check if skill already exists
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip existingChip = (Chip) child;
                if (existingChip.getText().toString().equalsIgnoreCase(skillText)) {
                    return; // Skill already exists
                }
            }
        }

        Chip skillChip = new Chip(this);
        skillChip.setText(skillText);
        skillChip.setCloseIconVisible(true);
        skillChip.setClickable(true);
        skillChip.setCheckable(false);
        skillChip.setOnCloseIconClickListener(v -> chipGroup.removeView(skillChip));
        chipGroup.addView(skillChip);
    }

    private void setupLanguageInput() {
        AutoCompleteTextView languageDropdown = findViewById(R.id.spinnerLanguage);
        AutoCompleteTextView proficiencyDropdown = findViewById(R.id.spinnerProficiency);
        MaterialButton addLanguageBtn = findViewById(R.id.btnAddLanguage);
        LinearLayout languageContainer = findViewById(R.id.languageContainer);

        // Language options
        String[] languages = {"English", "Spanish", "French", "German", "Chinese (Mandarin)", "Japanese", "Korean", "Portuguese", "Italian", "Arabic", "Russian", "Hindi"};
        if (languageDropdown != null) {
            ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, languages);
            languageDropdown.setAdapter(languageAdapter);
        }

        // Proficiency levels
        String[] proficiencies = {"Native", "Fluent", "Intermediate", "Basic"};
        if (proficiencyDropdown != null) {
            ArrayAdapter<String> proficiencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, proficiencies);
            proficiencyDropdown.setAdapter(proficiencyAdapter);
        }

        // Add language button
        if (addLanguageBtn != null) {
            addLanguageBtn.setOnClickListener(v -> {
                String language = languageDropdown != null ? languageDropdown.getText().toString().trim() : "";
                String proficiency = proficiencyDropdown != null ? proficiencyDropdown.getText().toString().trim() : "";
                
                if (!language.isEmpty() && !proficiency.isEmpty()) {
                    addLanguageItem(language, proficiency, languageContainer);
                    languageDropdown.setText("");
                    proficiencyDropdown.setText("");
                } else {
                    Toast.makeText(this, "Please select both language and proficiency", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addLanguageItem(String language, String proficiency, LinearLayout container) {
        // Check if language already exists
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag() != null && child.getTag().toString().equals(language)) {
                Toast.makeText(this, "Language already added", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create language item layout
        LinearLayout languageItem = new LinearLayout(this);
        languageItem.setOrientation(LinearLayout.HORIZONTAL);
        languageItem.setTag(language);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16);
        languageItem.setLayoutParams(layoutParams);

        // Language text
        TextView languageText = new TextView(this);
        languageText.setText(language + " (" + proficiency + ")");
        languageText.setTextSize(14);
        languageText.setTextColor(getResources().getColor(android.R.color.black));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        languageText.setLayoutParams(textParams);

        // Remove button
        MaterialButton removeBtn = new MaterialButton(this);
        removeBtn.setText("Remove");
        removeBtn.setTextSize(12);
        removeBtn.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        removeBtn.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        removeBtn.setLayoutParams(btnParams);
        removeBtn.setOnClickListener(v -> container.removeView(languageItem));

        languageItem.addView(languageText);
        languageItem.addView(removeBtn);
        container.addView(languageItem);
    }

    private void createResume() {
        // Validate required fields
        if (!validateRequiredSections()) {
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating your resume...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Collect all resume data
        String resumeData = collectResumeData();

        // Call API to generate resume
        ApiService apiService = new ApiService(this);
        apiService.generateResume(resumeData, new ApiService.FileCallback() {
            @Override
            public void onSuccess(File file) {
                progressDialog.dismiss();
                showSuccessDialog();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                showErrorDialog(error);
            }
        });
    }

    private boolean validateRequiredSections() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // Personal Info - Name is required
        String fullName = getFieldText(R.id.etName);
        if (fullName.isEmpty()) {
            errors.append("• Full name is required\n");
            isValid = false;
        }

        String email = getFieldText(R.id.etEmail);
        if (email.isEmpty()) {
            errors.append("• Email is required\n");
            isValid = false;
        }

        // At least one of Experience or Education
        if (!hasExperienceData() && !hasEducationData()) {
            errors.append("• Please fill either Experience or Education section\n");
            isValid = false;
        }

        if (!isValid) {
            showValidationDialog(errors.toString());
        }

        return isValid;
    }

    private boolean hasExperienceData() {
        String company = getFieldText(R.id.etCompanyExp);
        return !company.isEmpty();
    }

    private boolean hasEducationData() {
        String university = getFieldText(R.id.etUniversityEdu);
        String degree = getFieldText(R.id.etDegreeEdu);
        return !university.isEmpty() || !degree.isEmpty();
    }

    private String getFieldText(int fieldId) {
        EditText field = findViewById(fieldId);
        return field != null ? field.getText().toString().trim() : "";
    }

    private String getDropdownValue(int dropdownId) {
        AutoCompleteTextView dropdown = findViewById(dropdownId);
        return dropdown != null ? dropdown.getText().toString().trim() : "";
    }

    private String collectResumeData() {
        JsonObject resumeData = new JsonObject();

        // Personal Information
        JsonObject personalInfo = new JsonObject();
        personalInfo.addProperty("name", getFieldText(R.id.etName));
        personalInfo.addProperty("email", getFieldText(R.id.etEmail));
        personalInfo.addProperty("phone", getFieldText(R.id.etContactNo));
        personalInfo.addProperty("address", ""); // No address field in current layout
        resumeData.add("personal_info", personalInfo);

        // Objectives
        JsonObject objectives = new JsonObject();
        objectives.addProperty("industry", getDropdownValue(R.id.spinnerIndustryObj));
        objectives.addProperty("level_of_work", getDropdownValue(R.id.spinnerLevelOfWorkObj));
        objectives.addProperty("company_scale", getDropdownValue(R.id.spinnerCompanyScaleObj));
        resumeData.add("objectives", objectives);

        // Experience
        JsonObject experience = new JsonObject();
        experience.addProperty("job_title", ""); // No job title field in current layout
        experience.addProperty("company", getFieldText(R.id.etCompanyExp));
        experience.addProperty("from_date", getFieldText(R.id.etFromDateExp));
        experience.addProperty("to_date", getFieldText(R.id.etToDateExp));
        experience.addProperty("responsibilities", getFieldText(R.id.etResponsibilitiesExp));
        
        CheckBox currentJobCheckbox = findViewById(R.id.cbCurrentJobExp);
        experience.addProperty("is_current_job", currentJobCheckbox != null && currentJobCheckbox.isChecked());
        resumeData.add("experience", experience);

        // Education
        JsonObject education = new JsonObject();
        education.addProperty("university", getFieldText(R.id.etUniversityEdu));
        education.addProperty("degree", getFieldText(R.id.etDegreeEdu));
        education.addProperty("from_date", getFieldText(R.id.etFromDateEdu));
        education.addProperty("to_date", getFieldText(R.id.etToDateEdu));
        education.addProperty("background", getFieldText(R.id.etBackgroundEdu));
        resumeData.add("education", education);

        // Skills
        List<String> skillsList = new ArrayList<>();
        ChipGroup skillsChipGroup = findViewById(R.id.chipGroupSkills);
        if (skillsChipGroup != null) {
            for (int i = 0; i < skillsChipGroup.getChildCount(); i++) {
                View child = skillsChipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    skillsList.add(((Chip) child).getText().toString());
                }
            }
        }
        resumeData.addProperty("skills", String.join(", ", skillsList));

        // Languages
        List<String> languagesList = new ArrayList<>();
        LinearLayout languageContainer = findViewById(R.id.languageContainer);
        if (languageContainer != null) {
            for (int i = 0; i < languageContainer.getChildCount(); i++) {
                View child = languageContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout languageItem = (LinearLayout) child;
                    if (languageItem.getChildCount() > 0 && languageItem.getChildAt(0) instanceof TextView) {
                        TextView languageText = (TextView) languageItem.getChildAt(0);
                        languagesList.add(languageText.getText().toString());
                    }
                }
            }
        }
        resumeData.addProperty("languages", String.join(", ", languagesList));

        return new Gson().toJson(resumeData);
    }

    private void showValidationDialog(String errors) {
        new AlertDialog.Builder(this)
            .setTitle("Please Complete Required Fields")
            .setMessage(errors)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Resume Created Successfully!")
            .setMessage("Your resume has been generated and saved. You can view it in your Resume Library.")
            .setPositiveButton("Go to Resume Library", (dialog, which) -> {
                Intent intent = new Intent(this, ResumeLibraryActivity.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Stay Here", null)
            .show();
    }

    private void showErrorDialog(String error) {
        new AlertDialog.Builder(this)
            .setTitle("Resume Creation Failed")
            .setMessage(error)
            .setPositiveButton("Try Again", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
