package uk.gov.hmcts.opal.entity.businessunit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;

import java.util.List;

@Entity
@Table(name = "business_units")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitLiteEntity extends BusinessUnitEntity {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "business_unit_id")
    private List<ConfigurationItemEntity.Lite> configurationItems;
}
