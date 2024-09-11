package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class NotesApiStepDef extends BaseStepDef {

    @When("I post the following data to the notes API")
    public void postToNotesApi(DataTable notesFields) throws JSONException {
        Map<String, String> dataToPost = notesFields.asMap(String.class, String.class);

        JSONObject body = new JSONObject();
        body.put("associatedRecordId", dataToPost.get("recordId"));
        body.put("associatedRecordType", dataToPost.get("recordType"));
        body.put("noteText", dataToPost.get("noteText"));
        body.put("noteType", dataToPost.get("noteType"));
        body.put("postedBy", dataToPost.get("postedBy"));
        body.put("postedDate", dataToPost.get("postedDate"));
        if (dataToPost.get("noteId") != null) {
            body.put("noteId", dataToPost.get("noteId"));
        }
        SerenityRest.given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .body(body.toString())
            .when()
            .post(getTestUrl() + "/notes");
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
