/**
 * PURPOSE: contains student and test table schemas
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.database;

public class MathSchemas
{
    public static class StudentTable
    {
        //Relational schema's table with 5 attributes:
        public static final String NAME = "student";
        public static final class Cols{
            public static final String FNAME = "first_name";
            public static final String LNAME = "last_name";
            public static final String PHONE = "phone_number";
            public static final String EMAIL = "email";
            public static final String PICTURE = "profile_picture";
        }
    }

    public static class TestTable
    {
        //Relational schema's table with 4 attributes:
        public static final String NAME = "test";
        public static final class Cols{
            public static final String STUDENT = "student_name";
            public static final String SCORE = "test_score";
            public static final String DATE = "test_date";
            public static final String TIME = "test_duration";
        }
    }
}
