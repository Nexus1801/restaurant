package com.example.restaurant.constant;

public abstract class SQLCommand {

    // Query 1: Customer Order History
    public static String QUERY_CUSTOMER_ORDER_HISTORY =
            "SELECT c.Cust_name, o.Order_id, o.Order_status, o.Order_Total, " +
                    "oi.Orditem_Menu_dishID, m.Menu_name, oi.Orditem_Quantity, " +
                    "oi.Orditem_price, oi.Orditem_time_ordered " +
                    "FROM Customer c " +
                    "JOIN Orders o ON c.Cust_id = o.Order_Cust_id " +
                    "JOIN Order_Item oi ON o.Order_id = oi.Orditem_Order_id " +
                    "JOIN Menu m ON oi.Orditem_Menu_dishID = m.Menu_DishID " +
                    "WHERE c.Cust_id = ? " +
                    "ORDER BY oi.Orditem_time_ordered DESC";

    // Query 2: Reservation Screen
    public static String QUERY_RESERVATIONS =
            "SELECT r.Res_id, c.Cust_name, c.Cust_Number, dt.DT_number, " +
                    "dt.DT_table_type, r.Res_date, r.Res_start_item, " +
                    "r.Res_party_size, r.Res_status " +
                    "FROM Reservation r " +
                    "JOIN Customer c ON r.Res_Cust_id = c.Cust_id " +
                    "JOIN Dining_table dt ON r.Res_DT_id = dt.DT_id " +
                    "WHERE r.Res_status = 'PENDING' " +
                    "ORDER BY r.Res_date, r.Res_start_item";

    // Query 3: Active Orders by Status
    public static String QUERY_ACTIVE_ORDERS =
            "SELECT o.Order_id, dt.DT_number AS Table_Number, c.Cust_name, " +
                    "e.Emp_name AS Server_Name, o.Order_status, o.Order_Total, " +
                    "GROUP_CONCAT(m.Menu_name || ' (x' || oi.Orditem_Quantity || ')') AS Items " +
                    "FROM Orders o " +
                    "LEFT JOIN Customer c ON o.Order_Cust_id = c.Cust_id " +
                    "JOIN Dining_table dt ON o.Order_DT_id = dt.DT_id " +
                    "JOIN Employee e ON o.Order_Emp_id = e.Emp_id " +
                    "JOIN Order_Item oi ON o.Order_id = oi.Orditem_Order_id " +
                    "JOIN Menu m ON oi.Orditem_Menu_dishID = m.Menu_DishID " +
                    "WHERE o.Order_status IN ('Placed', 'Preparing', 'Ready') " +
                    "GROUP BY o.Order_id " +
                    "ORDER BY oi.Orditem_time_ordered";

    // Query 4: Menu Items by Availability
    public static String QUERY_MENU_AVAILABLE =
            "SELECT Menu_DishID, Menu_name, Menu_cat AS Category, " +
                    "Menu_price, Menu_available " +
                    "FROM Menu " +
                    "WHERE Menu_available = 'Yes' " +
                    "ORDER BY Menu_cat, Menu_name";

    // Query 5a: Top 5 Best Selling Items
    public static String QUERY_TOP_SELLING =
            "SELECT m.Menu_name, m.Menu_cat, " +
                    "SUM(oi.Orditem_Quantity) AS Total_Sold, " +
                    "SUM(oi.Orditem_Quantity * oi.Orditem_price) AS Revenue " +
                    "FROM Menu m " +
                    "JOIN Order_Item oi ON m.Menu_DishID = oi.Orditem_Menu_dishID " +
                    "GROUP BY m.Menu_DishID, m.Menu_name, m.Menu_cat " +
                    "ORDER BY Total_Sold DESC " +
                    "LIMIT 5";

    // Query 5b: Most Frequent Party Size
    public static String QUERY_FREQUENT_PARTY_SIZE =
            "SELECT Res_party_size AS Party_Size, COUNT(*) AS Frequency " +
                    "FROM Reservation " +
                    "GROUP BY Res_party_size " +
                    "ORDER BY Frequency DESC " +
                    "LIMIT 1";

    // Query 6: Employee Shift Schedule
    public static String QUERY_EMPLOYEE_SHIFTS =
            "SELECT e.Emp_id, e.Emp_name, e.Emp_role, " +
                    "s.Shift_start_time, s.Shift_end_time, " +
                    "CAST((julianday(s.Shift_end_time) - julianday(s.Shift_start_time)) * 24 AS INTEGER) AS Hours_Worked " +
                    "FROM Employee e " +
                    "JOIN Shift s ON e.Emp_id = s.Shift_Emp_id " +
                    "ORDER BY s.Shift_start_time";

    // Query 7: Available Tables by Party Size
    public static String QUERY_AVAILABLE_TABLES =
            "SELECT DT_id, DT_number, DT_party_size, DT_table_type " +
                    "FROM Dining_table " +
                    "WHERE DT_party_size >= ? " +
                    "ORDER BY DT_party_size";

    // Query 8: Inventory Alerts (Low Stock)
    public static String QUERY_LOW_INVENTORY =
            "SELECT i.Inv_ItemID, m.Menu_name, " +
                    "i.Inv_item_count AS Current_Stock, " +
                    "i.Inv_Hard_restock AS Restock_Level " +
                    "FROM Inventory i " +
                    "JOIN Menu m ON i.Inv_Menu_dishID = m.Menu_DishID " +
                    "WHERE i.Inv_item_count <= CAST(i.Inv_Hard_restock AS INTEGER) " +
                    "ORDER BY i.Inv_item_count";

    // Query 9a: Highest Payment
    public static String QUERY_HIGHEST_PAYMENT =
            "SELECT Pay_id, Pay_Order_id, o.Order_Total AS Amount, Pay_time " +
                    "FROM Payments p " +
                    "JOIN Orders o ON p.Pay_Order_id = o.Order_id " +
                    "ORDER BY o.Order_Total DESC LIMIT 1";

    // Query 9b: Lowest Payment
    public static String QUERY_LOWEST_PAYMENT =
            "SELECT Pay_id, Pay_Order_id, o.Order_Total AS Amount, Pay_time " +
                    "FROM Payments p " +
                    "JOIN Orders o ON p.Pay_Order_id = o.Order_id " +
                    "ORDER BY o.Order_Total ASC LIMIT 1";

    // Query 9c: Mean Payment
    public static String QUERY_MEAN_PAYMENT =
            "SELECT ROUND(AVG(o.Order_Total), 2) AS Average_Payment " +
                    "FROM Payments p " +
                    "JOIN Orders o ON p.Pay_Order_id = o.Order_id";

    // Query 9d: Median Payment
    public static String QUERY_MEDIAN_PAYMENT =
            "SELECT AVG(Order_Total) AS Median_Payment " +
                    "FROM (SELECT o.Order_Total FROM Payments p " +
                    "JOIN Orders o ON p.Pay_Order_id = o.Order_id " +
                    "ORDER BY o.Order_Total " +
                    "LIMIT 2 - (SELECT COUNT(*) FROM Payments) % 2 " +
                    "OFFSET (SELECT (COUNT(*) - 1) / 2 FROM Payments))";

    // Query 9e: Days of Week with Highest Payments
    public static String QUERY_PAYMENT_BY_DAY =
            "SELECT CASE CAST(strftime('%w', Pay_time) AS INTEGER) " +
                    "WHEN 0 THEN 'Sunday' WHEN 1 THEN 'Monday' " +
                    "WHEN 2 THEN 'Tuesday' WHEN 3 THEN 'Wednesday' " +
                    "WHEN 4 THEN 'Thursday' WHEN 5 THEN 'Friday' " +
                    "WHEN 6 THEN 'Saturday' END AS Day_of_Week, " +
                    "SUM(o.Order_Total) AS Total_Revenue, " +
                    "COUNT(*) AS Number_of_Payments " +
                    "FROM Payments p " +
                    "JOIN Orders o ON p.Pay_Order_id = o.Order_id " +
                    "GROUP BY strftime('%w', Pay_time) " +
                    "ORDER BY Total_Revenue DESC";

    // Query 10: Order Details with Items
    public static String QUERY_ORDER_DETAILS =
            "SELECT o.Order_id, c.Cust_name, dt.DT_number AS Table_Number, " +
                    "m.Menu_name, oi.Orditem_Quantity, oi.Orditem_price, " +
                    "(oi.Orditem_Quantity * oi.Orditem_price) AS Line_Total, " +
                    "oi.Orditem_time_ordered " +
                    "FROM Orders o " +
                    "LEFT JOIN Customer c ON o.Order_Cust_id = c.Cust_id " +
                    "JOIN Dining_table dt ON o.Order_DT_id = dt.DT_id " +
                    "JOIN Order_Item oi ON o.Order_id = oi.Orditem_Order_id " +
                    "JOIN Menu m ON oi.Orditem_Menu_dishID = m.Menu_DishID " +
                    "WHERE o.Order_id = ? " +
                    "ORDER BY oi.Orditem_time_ordered";

    // Query 11: Customer Reservation History
    public static String QUERY_CUSTOMER_RESERVATIONS =
            "SELECT c.Cust_name, r.Res_id, r.Res_date, r.Res_start_item, " +
                    "r.Res_party_size, dt.DT_number, r.Res_status " +
                    "FROM Customer c " +
                    "JOIN Reservation r ON c.Cust_id = r.Res_Cust_id " +
                    "JOIN Dining_table dt ON r.Res_DT_id = dt.DT_id " +
                    "WHERE c.Cust_id = ? " +
                    "ORDER BY r.Res_date DESC";

    // Query 12: Employee Section Assignments
    public static String QUERY_SECTION_ASSIGNMENTS =
            "SELECT rs.Sec_id, e.Emp_name, e.Emp_role, " +
                    "COUNT(DISTINCT dt.DT_id) AS Tables_Assigned, " +
                    "COUNT(DISTINCT o.Order_id) AS Active_Orders " +
                    "FROM Restaurant_Section rs " +
                    "JOIN Employee e ON rs.Sec_Emp_id = e.Emp_id " +
                    "JOIN Dining_table dt ON rs.Sec_DT_id = dt.DT_id " +
                    "LEFT JOIN Orders o ON dt.DT_id = o.Order_DT_id " +
                    "AND o.Order_status IN ('Placed', 'Preparing', 'Ready') " +
                    "GROUP BY rs.Sec_id, e.Emp_name, e.Emp_role";

    // Query 13: Peak Order Times
    public static String QUERY_PEAK_TIMES =
            "SELECT strftime('%H:00', Orditem_time_ordered) AS Hour_Interval, " +
                    "COUNT(DISTINCT Orditem_Order_id) AS Number_of_Orders, " +
                    "SUM(Orditem_Quantity) AS Total_Items_Ordered " +
                    "FROM Order_Item " +
                    "GROUP BY strftime('%H', Orditem_time_ordered) " +
                    "ORDER BY Number_of_Orders DESC " +
                    "LIMIT 5";

    // Additional useful queries for the app

    // Get all tables
    public static String QUERY_ALL_TABLES =
            "SELECT DT_id, DT_number, DT_party_size, DT_table_type " +
                    "FROM Dining_table ORDER BY DT_number";

    // Get menu by category
    public static String QUERY_MENU_BY_CATEGORY =
            "SELECT * FROM Menu WHERE Menu_cat = ? AND Menu_available = 'Yes' " +
                    "ORDER BY Menu_name";

    // Employee login validation
    public static String QUERY_EMPLOYEE_LOGIN =
            "SELECT Emp_id, Emp_name, Emp_role FROM Employee WHERE Emp_id = ?";

    // Insert new order
    public static String INSERT_ORDER =
            "INSERT INTO Orders(Order_id, Order_Cust_id, Order_DT_id, " +
                    "Order_Emp_id, Order_status, Order_Total) VALUES(?, ?, ?, ?, ?, ?)";

    // Insert order item
    public static String INSERT_ORDER_ITEM =
            "INSERT INTO Order_Item(Orditem_id, Orditem_Order_id, " +
                    "Orditem_Menu_dishID, Orditem_Quantity, Orditem_price, " +
                    "Orditem_time_ordered) VALUES(?, ?, ?, ?, ?, ?)";

    // Update order status
    public static String UPDATE_ORDER_STATUS =
            "UPDATE Orders SET Order_status = ? WHERE Order_id = ?";

    // Update inventory
    public static String UPDATE_INVENTORY =
            "UPDATE Inventory SET Inv_item_count = ? WHERE Inv_ItemID = ?";

    // Get order total
    public static String QUERY_ORDER_TOTAL =
            "SELECT SUM(Orditem_Quantity * Orditem_price) AS Total " +
                    "FROM Order_Item WHERE Orditem_Order_id = ?";
}






