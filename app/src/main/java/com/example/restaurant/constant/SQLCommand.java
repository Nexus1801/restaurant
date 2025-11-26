package com.example.restaurant.constant;

public abstract class SQLCommand {
    //query all students
    public static String QUERY_STUDENT;
    //list all data in books table
    public static String QUERY_1;
    //List the call numbers of books with the title
    public static String QUERY_2;
    public static String QUERY_3;
    public static String QUERY_4;
    public static String QUERY_5;
    public static String QUERY_6;
    public static String QUERY_7;

    static {
        QUERY_STUDENT = "select stid, stname from Student";
        QUERY_1 = "select lbcallnum, lbtitle from Libbook";
        QUERY_2 = "select lbcallnum from libbook where lbtitle like '%Database Management%'";
        QUERY_3 = "select * from Student";
        QUERY_4 = "select * from Libbook";
        QUERY_5 = "select * from Student";
        QUERY_6 = "select * from Libbook";
        QUERY_7 = "select * from Libbook";
    }

    public static String RETURN_BOOK = "update checkout set coreturned=? where stid=? and lbcallnum=?";
    public static String CHECK_BOOK = "insert into checkout(stid,lbcallnum,coduedate,coreturned) values(?,?,?,?)";
    public static String CHECKOUT_SUMMARY = "select strftime('%m',coDueDate) as month,count(*) as total from checkout where strftime('%Y',coDueDate)='2017' group by month order by total desc";
    public static String CHECKOUT_LIST = "select CheckOut.stdID as s_id, lbTitle,coDueDate,coIsReturned,coFine,stdFirstName from CheckOut,Student,LibBook where Student.stdID=Checkout.stdID and LibBook.lbCallNum=CheckOut.lbCallNum";
}






