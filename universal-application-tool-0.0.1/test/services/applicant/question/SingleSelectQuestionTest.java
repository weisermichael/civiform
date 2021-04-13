package services.applicant.question;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Optional;
import models.Applicant;
import models.LifecycleStage;
import org.junit.Before;
import org.junit.Test;
import services.Path;
import services.applicant.ApplicantData;
import services.question.types.DropdownQuestionDefinition;

public class SingleSelectQuestionTest {

  private static final DropdownQuestionDefinition dropdownQuestionDefinition =
      new DropdownQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"),
          ImmutableListMultimap.of(
              Locale.US,
              "option 1",
              Locale.US,
              "option 2",
              Locale.FRANCE,
              "un",
              Locale.FRANCE,
              "deux"));

  private Applicant applicant;
  private ApplicantData applicantData;

  @Before
  public void setUp() {
    applicant = new Applicant();
    applicantData = applicant.getApplicantData();
  }

  @Test
  public void withEmptyApplicantData() {
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, applicantData);

    SingleSelectQuestion singleSelectQuestion = new SingleSelectQuestion(applicantQuestion);

    assertThat(singleSelectQuestion.getOptions()).containsOnly("option 1", "option 2");
    assertThat(applicantQuestion.hasErrors()).isFalse();
  }

  @Test
  public void withPresentApplicantData() {
    applicantData.putString(dropdownQuestionDefinition.getSelectionPath(), "option 1");
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, applicantData);

    SingleSelectQuestion singleSelectQuestion = applicantQuestion.createSingleSelectQuestion();

    assertThat(singleSelectQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(singleSelectQuestion.hasQuestionErrors()).isFalse();
    assertThat(singleSelectQuestion.getSelectedOptionValue()).hasValue("option 1");
  }

  @Test
  public void withPresentApplicantData_selectedInvalidOption_hasErrors() {
    applicantData.putString(
        dropdownQuestionDefinition.getSelectionPath(), "this isn't a valid answer!");
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, applicantData);

    SingleSelectQuestion singleSelectQuestion = applicantQuestion.createSingleSelectQuestion();

    assertThat(singleSelectQuestion.hasTypeSpecificErrors()).isTrue();
    assertThat(singleSelectQuestion.hasQuestionErrors()).isFalse();
    assertThat(singleSelectQuestion.getSelectedOptionValue())
        .hasValue("this isn't a valid answer!");
  }
}