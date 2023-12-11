package uk.gov.hmcts.opal.util;

public class NamesUtil {


    /** Example format expected: "Smith , Mr JJ". */
    public static String[] splitFullName(String fullName) {

        // Splitting by comma to separate surname and rest of the name
        String[] nameSplit = fullName.split(",\s+", 2);

        // Extracting surname
        String surname = nameSplit[0].trim();

        // Extracting forenames and title
        String forenamesWithTitle = nameSplit[1];
        int lastSpaceIndex = forenamesWithTitle.lastIndexOf(" ");

        // Extracting title (if available)
        String title = lastSpaceIndex != -1 ? forenamesWithTitle.substring(lastSpaceIndex + 1) : "";

        // Extracting forenames
        String forenames = forenamesWithTitle.substring(0, lastSpaceIndex).trim();

        // Returning the results in an array
        return new String[]{surname, forenames, title};
    }
}
