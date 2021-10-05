import { startSession, loginAsProgramAdmin, loginAsAdmin, AdminQuestions, AdminPrograms, endSession, logout, loginAsTestUser, selectApplicantLanguage, ApplicantQuestions, userDisplayName } from './support'

describe('optional application flow', () => {
  it('program with all question types', async () => {
    const { browser, page } = await startSession()
    page.setDefaultTimeout(5000);

    await loginAsAdmin(page);
    const adminQuestions = new AdminQuestions(page);
    const adminPrograms = new AdminPrograms(page);

    const questions = await adminQuestions.addAllNonSingleBlockQuestionTypes('optional-');
    await adminQuestions.addFileUploadQuestion({questionName: 'optional-file-upload'});

    const programName = 'Optional Questions Program';
    await adminPrograms.addProgram(programName);
    await adminPrograms.editProgramBlockWithOptional(programName, 'first description', [], 'optional-file-upload');

    for (let i = 0; i < questions.length; i++) {
      await adminPrograms.addProgramBlockWithOptional(programName, 'description', [], questions[i]);
    }

    const programName2 = 'Second Optional Questions Program';
    await adminPrograms.addProgram(programName2);
    await adminPrograms.editProgramBlockWithOptional(programName2, 'first description', [], 'optional-file-upload');

    await adminPrograms.gotoAdminProgramsPage();
    await adminPrograms.expectDraftProgram(programName);
    await adminPrograms.expectDraftProgram(programName2);

    await adminPrograms.publishAllPrograms();
    await adminPrograms.expectActiveProgram(programName);
    await adminPrograms.expectActiveProgram(programName2);

    await logout(page);
    await loginAsTestUser(page);
    await selectApplicantLanguage(page, 'English');

    const applicantQuestions = new ApplicantQuestions(page);
    await applicantQuestions.applyProgram(programName);

    // Skip first block without uploading a file
    await applicantQuestions.clickSkip();
    // Skip blocks 2-5 without answering any questions
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();

    //Skip blocks 6-10 without answering any questions
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();
    await applicantQuestions.clickNext();

    // Submit the first program
    await applicantQuestions.submitFromReviewPage(programName);

    // Complete the second program
    await applicantQuestions.applyProgram(programName2);

    // Skip Screen 1 when it pops up to be answered again
    await applicantQuestions.clickSkip();

    // Submit from review page
    await applicantQuestions.submitFromReviewPage(programName2);
    await endSession(browser);
  })
})
