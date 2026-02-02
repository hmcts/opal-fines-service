package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpecificationUtilsTest {

    @Test
    void stripCheckLetter_null_returnsNull() {
        assertNull(SpecificationUtils.stripCheckLetter(null));
    }

    @Test
    void stripCheckLetter_length9_withTrailingLetter_stripsLastChar() {
        assertEquals("12345678", SpecificationUtils.stripCheckLetter("12345678A"));
    }

    @Test
    void stripCheckLetter_length9_withTrailingDigit_leavesIntact() {
        assertEquals("123456789", SpecificationUtils.stripCheckLetter("123456789"));
    }

    @Test
    void stripCheckLetter_otherLengths_leavesIntact() {
        assertEquals("1234567", SpecificationUtils.stripCheckLetter("1234567"));
        assertEquals("1234567890", SpecificationUtils.stripCheckLetter("1234567890"));
    }

    @Test
    void normalize_lowercasesAndStripsConfiguredChars() {
        String raw = " A-b.C,'/\\(D)[E]{F}:;!?\"@# $%&*+ ";
        assertEquals("abcdef", SpecificationUtils.normalize(raw));
    }

    @Test
    void normalize_keepsCharactersNotInStripList() {
        assertEquals("ab_c", SpecificationUtils.normalize("Ab_C"));
    }

    @Test
    void escapeForLike_escapesBackslashPercentUnderscore() {
        String raw = "a\\b%c_d";
        assertEquals("a\\\\b\\%c\\_d", SpecificationUtils.escapeForLike(raw));
    }

    @Test
    void escapeForLike_idempotentForSafeChars() {
        assertEquals("safeText", SpecificationUtils.escapeForLike("safeText"));
    }

    @Test
    void hasText_various() {
        assertFalse(SpecificationUtils.hasText(null));
        assertFalse(SpecificationUtils.hasText(""));
        assertFalse(SpecificationUtils.hasText("   "));
        assertTrue(SpecificationUtils.hasText("  x "));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void likeStartsWithNormalized_withRoot_buildsLikeWithNormalizedEscapedPrefix() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<?> root = mock(Root.class);

        Path<Object> path = mock(Path.class);

        Expression<String> chain = mock(Expression.class);
        Predicate expected = mock(Predicate.class);

        when(root.get("name")).thenReturn(path);
        when(cb.lower(any())).thenReturn(chain);
        when(cb.function(eq("REPLACE"), eq(String.class), any(), any(), any()))
            .thenReturn(chain);

        when(cb.like(any(), anyString(), anyChar())).thenReturn(expected);

        String raw = " A-b%_ ";
        Predicate p = SpecificationUtils.likeStartsWithNormalized(root, cb, "name", raw);

        assertSame(expected, p);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(cb).like(any(), cap.capture(), eq('\\'));
        assertEquals("ab\\_%", cap.getValue()); // normalize -> "ab_", escape -> "ab\\_", then add "%"
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void likeStartsWithNormalized_withExpression_buildsLikeWithNormalizedEscapedPrefix() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Expression<String> expr = mock(Expression.class);
        Expression<String> lowered = mock(Expression.class);
        Expression<String> replaced = mock(Expression.class);
        Predicate expected = mock(Predicate.class);

        when(cb.lower(any())).thenReturn(lowered);
        when(cb.function(eq("REPLACE"), eq(String.class), any(), any(), any())).thenReturn(replaced);
        when(cb.like(any(), anyString(), anyChar())).thenReturn(expected);

        String raw = "Foo_Bar%";
        Predicate p = SpecificationUtils.likeStartsWithNormalized(cb, expr, raw);

        assertSame(expected, p);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(cb).like(any(), cap.capture(), eq('\\'));
        assertEquals("foo\\_bar%", cap.getValue());
    }
}
