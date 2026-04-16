package uk.gov.hmcts.opal.entity.defendantaccount;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;


public class AssociationTypeUserType implements UserType<AssociationType> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<AssociationType> returnedClass() {
        return AssociationType.class;
    }

    @Override
    public boolean equals(AssociationType x, AssociationType y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(AssociationType x) {
        return Objects.hashCode(x);
    }

    @Override
    public AssociationType nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        String label = rs.getString(position);
        return rs.wasNull() ? null : AssociationType.getByLabel(label);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, AssociationType value, int index, WrapperOptions options)
        throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        PGobject pgObject = new PGobject();
        pgObject.setType("t_association_type_enum");
        pgObject.setValue(value.getLabel());
        st.setObject(index, pgObject);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public AssociationType deepCopy(AssociationType value) {
        return value;
    }

    @Override
    public Serializable disassemble(AssociationType value) {
        return value;
    }

    @Override
    public AssociationType assemble(Serializable cached, Object owner) {
        return (AssociationType) cached;
    }

    @Override
    public AssociationType replace(AssociationType detached, AssociationType managed, Object owner) {
        return detached;
    }
}