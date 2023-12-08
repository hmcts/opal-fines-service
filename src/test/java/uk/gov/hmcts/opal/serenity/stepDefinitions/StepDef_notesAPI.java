package uk.gov.hmcts.opal.serenity.stepDefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import static net.serenitybdd.rest.SerenityRest.*;

import java.util.Map;

public class StepDef_notesAPI {
    String testURL = System.getenv("TEST_URL");

    public String ifUrlNullSetLocal(){
        if(testURL == null){
            testURL = "http://localhost:4550";
            System.out.println("Set to: " + testURL);
        }
        else return testURL;
        return testURL;
    }

    @When("I post the following data to the notes API")
    public void iPostTheFollowingToNoteApi(DataTable notesFields) throws JSONException {
        Map<String, String> dataToPost = notesFields.asMap(String.class, String.class);

        JSONObject body = new JSONObject();
        body.put("associatedRecordId", dataToPost.get("recordId"));
        body.put("associatedRecordType", dataToPost.get("recordType"));
        body.put("noteText", dataToPost.get("noteText"));
        body.put("noteType", dataToPost.get("noteType"));
        body.put("postedBy", dataToPost.get("postedBy"));
        body.put("postedDate", dataToPost.get("postedDate"));
        if(!(dataToPost.get("noteId") == null)){
            body.put("noteId", dataToPost.get("noteId"));
        }
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(body.toString())
            .when()
            .post(ifUrlNullSetLocal() + "/api/notes");
        then().assertThat()
            .statusCode(201)
            .body("associatedRecordId", Matchers.equalTo(dataToPost.get("recordId")))
            .body("associatedRecordType", Matchers.equalTo(dataToPost.get("recordType")))
            .body("noteText", Matchers.equalTo(dataToPost.get("noteText")))
            .body("noteType", Matchers.equalTo(dataToPost.get("noteType")))
            .body("postedBy", Matchers.equalTo(dataToPost.get("postedBy")));
            //.body("postedDate", equalTo(dataToPost.get("postedDate")));
    }

}
