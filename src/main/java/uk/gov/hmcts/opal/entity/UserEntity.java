package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    @EqualsAndHashCode.Exclude
    private String username;

    @Column(name = "password", length = 1000)
    @EqualsAndHashCode.Exclude
    private String password;

    @Column(name = "description", length = 100)
    @EqualsAndHashCode.Exclude
    private String description;

}
