package org.upgrad.upstac.testrequests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class LabRequestControllerTest {

	@Autowired
	LabRequestController labRequestController;
	
	@Autowired
	TestRequestQueryService testRequestQueryService;

	
	
	// @Test
	// @WithUserDetails(value = "tester")
	public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);

		TestRequest updatedTestRequest = labRequestController.assignForLabTest(testRequest.getRequestId());
		assertThat(updatedTestRequest.getRequestId(), equalTo(testRequest.getRequestId()));
		assertThat(updatedTestRequest.getStatus(), equalTo(RequestStatus.LAB_TEST_IN_PROGRESS));
		assertNotNull(updatedTestRequest.getLabResult());
	}

	
	
	public TestRequest getTestRequestByStatus(RequestStatus status) {
		return testRequestQueryService.findBy(status).stream().findFirst().get();
	}

	
	
	// @Test
	// @WithUserDetails(value = "tester")
	public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception() {

		Long InvalidRequestId = -34L;

		AppException exception = assertThrows(AppException.class, () -> {
			labRequestController.assignForLabTest(InvalidRequestId);
		});

		assertThat(exception.getMessage(), containsString("Invalid ID"));

	}

	
	
	// @Test
	// @WithUserDetails(value = "tester")
	public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

		CreateLabResult createLabResult = getCreateLabResult(testRequest);
		TestRequest updatedTestRequest = labRequestController.updateLabTest(testRequest.getRequestId(),
				createLabResult);

		assertThat(updatedTestRequest.getRequestId(), equalTo(testRequest.getRequestId()));
		assertThat(updatedTestRequest.getStatus(), equalTo(RequestStatus.LAB_TEST_COMPLETED));
		assertThat(updatedTestRequest.getLabResult().getResult(), equalTo(createLabResult.getResult()));

	}

	
	
	// @Test
	// @WithUserDetails(value = "tester")
	public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

		CreateLabResult createLabResult = getCreateLabResult(testRequest);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			labRequestController.updateLabTest(-98L, createLabResult);

		});

		assertThat(exception.getMessage(), containsString("Invalid ID"));
	}

	
	
	// @Test
	// @WithUserDetails(value = "tester")
	public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception() {

		TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

		CreateLabResult createLabResult = getCreateLabResult(testRequest);

		createLabResult.setResult(null);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			labRequestController.updateLabTest(testRequest.getRequestId(), createLabResult);

		});

		assertThat(exception.getMessage(), containsString("ConstraintViolationException"));

	}

	
	
	public CreateLabResult getCreateLabResult(TestRequest testRequest) {

		CreateLabResult createLabResult = new CreateLabResult();
		createLabResult.setBloodPressure("130/90");
		createLabResult.setComments("Asymptomatic");
		createLabResult.setHeartBeat("90/95");
		createLabResult.setOxygenLevel("90-95");

		createLabResult.setTemperature("102");
		createLabResult.setComments("looks ok");
		createLabResult.setResult(TestStatus.NEGATIVE);

		return createLabResult;
	}

}