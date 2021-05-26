package org.upgrad.upstac.testrequests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class ConsultationControllerTest {

	@Autowired
	ConsultationController consultationController;
	
	@Autowired
	TestRequestQueryService testRequestQueryService;

	
	
	// @Test
	@WithUserDetails(value = "doctor")
	public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);

		TestRequest updatedTestRequest = consultationController.assignForConsultation(testRequest.getRequestId());

		assertThat(updatedTestRequest.getRequestId(), equalTo(testRequest.getRequestId()));
		assertThat(updatedTestRequest.getStatus(), equalTo(RequestStatus.DIAGNOSIS_IN_PROCESS));
		assertNotNull(updatedTestRequest.getConsultation());
	}

	
	
	public TestRequest getTestRequestByStatus(RequestStatus status) {
		return testRequestQueryService.findBy(status).stream().findFirst().get();
	}

	
	
	// @Test
	@WithUserDetails(value = "doctor")
	public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception() {

		Long InvalidRequestId = -34L;

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			consultationController.assignForConsultation(InvalidRequestId);
		});

		assertThat(exception.getMessage(), containsString("Invalid ID"));
	}

	
	
	// @Test
	@WithUserDetails(value = "doctor")
	public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

		CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
		TestRequest updatedTestRequest = consultationController.updateConsultation(testRequest.getRequestId(),
				createConsultationRequest);

		assertThat(updatedTestRequest.getRequestId(), equalTo(testRequest.getRequestId()));
		assertThat(updatedTestRequest.getStatus(), equalTo(RequestStatus.COMPLETED));

	}

	
	
	// @Test
	@WithUserDetails(value = "doctor")
	public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

		CreateConsultationRequest createLabResult = getCreateConsultationRequest(testRequest);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			consultationController.updateConsultation(-98L, createLabResult);

		});

		assertThat(exception.getMessage(), containsString("Invalid ID"));

	}

	
	
	// @Test
	@WithUserDetails(value = "doctor")
	public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

		CreateConsultationRequest createLabResult = getCreateConsultationRequest(testRequest);

		createLabResult.setSuggestion(null);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			consultationController.updateConsultation(testRequest.getRequestId(), createLabResult);

		});
		assertThat(exception.getMessage(), containsString("ConstraintViolationException"));
	}

	
	
	public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

		CreateLabResult createLabResult = new CreateLabResult();
		createLabResult.setBloodPressure("130/90");
		createLabResult.setComments("Asymptomatic");
		createLabResult.setHeartBeat("90/95");
		createLabResult.setOxygenLevel("90-95");

		createLabResult.setTemperature("102");
		createLabResult.setComments("looks ok");
		createLabResult.setResult(TestStatus.NEGATIVE);

		CreateConsultationRequest createConsultationRequest = new CreateConsultationRequest();
		createConsultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
		createConsultationRequest.setComments("suggestion");

		return createConsultationRequest;

	}

	
	
}