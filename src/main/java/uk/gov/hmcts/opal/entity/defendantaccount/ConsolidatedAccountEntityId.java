package uk.gov.hmcts.opal.entity.defendantaccount;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedAccountEntityId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long masterAccountId;

    private Long childAccountId;
}
