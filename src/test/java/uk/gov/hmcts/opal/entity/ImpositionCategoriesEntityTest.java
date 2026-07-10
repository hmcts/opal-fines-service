package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImpositionCategoriesEntityTest {

    @Test
    void gettersAndSettersExposeMappedFields() {
        ImpositionCategoriesEntity entity = new ImpositionCategoriesEntity();

        entity.setImpositionCategoryId("COMP");
        entity.setImpositionCategory("Compensation");
        entity.setItemNumber((short) 7);

        assertEquals("COMP", entity.getImpositionCategoryId());
        assertEquals("Compensation", entity.getImpositionCategory());
        assertEquals(Short.valueOf((short) 7), entity.getItemNumber());
    }

    @Test
    void tableAnnotationDeclaresNamedUniqueConstraintForImpositionCategory() {
        Table table = ImpositionCategoriesEntity.class.getAnnotation(Table.class);

        assertNotNull(table);
        assertEquals("imposition_categories", table.name());
        assertEquals(1, table.uniqueConstraints().length);

        UniqueConstraint uniqueConstraint = table.uniqueConstraints()[0];
        assertEquals("ic_imposition_category_uk", uniqueConstraint.name());
        assertArrayEquals(new String[]{"imposition_category"}, uniqueConstraint.columnNames());
    }
}
