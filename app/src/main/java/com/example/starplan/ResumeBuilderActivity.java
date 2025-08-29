package com.example.starplan;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
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
        findViewById(R.id.btnCreateResume).setOnClickListener(v -> 
            Toast.makeText(this, "Creating Resume - Coming Soon", Toast.LENGTH_SHORT).show());
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
