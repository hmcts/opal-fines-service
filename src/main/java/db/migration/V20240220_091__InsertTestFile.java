package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.IOException;

import static db.migration.CommonDbUtils.insertFile;

public class V20240220_091__InsertTestFile extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws IOException {
        insertFile(context, "someFileName", "db/files/sample.pdf");
    }
}
