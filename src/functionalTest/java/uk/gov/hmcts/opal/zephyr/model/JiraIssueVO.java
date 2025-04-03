package uk.gov.hmcts.opal.zephyr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class JiraIssueVO extends BaseJiraVO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("fields")
    private Fields fields;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Fields getFields() {
        return fields;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter @Setter
    public class Fields {

        @JsonProperty("issuetype")
        private IssueType issuetype;

        public IssueType getIssuetype() {
            return issuetype;
        }

        public void setIssuetype(IssueType issuetype) {
            this.issuetype = issuetype;
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Getter @Setter
        public class IssueType {

            @JsonProperty("id")
            private String id;

            @JsonProperty("name")
            private String name;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

    }
}
