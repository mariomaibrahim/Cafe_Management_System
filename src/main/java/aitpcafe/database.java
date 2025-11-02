package aitpcafe;

import java.sql.Connection;
import java.sql.DriverManager;

public class database {

    public static Connection connectDB() {
        try {
            // استخدام المشغل الحديث
            Class.forName("com.mysql.cj.jdbc.Driver");

            // الاتصال بقاعدة البيانات
            Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/cafe", "root", "");
            return connect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* database المستخدمة ضعيها في phpmyadmin/cafe
    
    SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

--
-- Database: `cafe`
--

-- --------------------------------------------------------

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `question` varchar(100) NOT NULL,
  `answer` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'cashier',
  `full_name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `salary` decimal(10,2) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `date` date DEFAULT NULL,
  `last_login` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `employee` (`id`, `username`, `password`, `question`, `answer`, `role`, `full_name`, `phone`, `email`, `salary`, `hire_date`, `status`, `date`, `last_login`) VALUES
(1, 'admin', '12345678', 'What is your favorite color?', 'Blue', 'admin', 'leader', '0123456789', 'admin@cafe.com', NULL, NULL, 'active', NULL, NULL),
(3, 'cashier1', '12345678', 'What is your favorite food?', 'pizza', 'cashier', 'testcashier', '0111111111', NULL, 3000.00, '2025-10-27', 'active', '2025-10-27', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `prod_id` varchar(50) NOT NULL,
  `prod_name` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `stock` int(11) NOT NULL,
  `price` double NOT NULL,
  `cost` double DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `min_stock_level` int(11) DEFAULT 10,
  `date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `prod_id` (`prod_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `product` (`id`, `prod_id`, `prod_name`, `type`, `stock`, `price`, `cost`, `status`, `image`, `description`, `min_stock_level`, `date`) VALUES
(1, '1', 'Espresso', 'Drinks', 50, 15, 5, 'Available', NULL, 'espriso', 10, '2025-11-01'),
(2, '2', 'Cappuccino', 'Drinks', 45, 20, 7, 'Available', NULL, 'cupetshino', 10, '2025-11-01'),
(3, '3', 'Latte', 'Drinks', 40, 22, 8, 'Available', NULL, 'latey', 10, '2025-11-01'),
(4, '4', 'Croissant', 'Food', 30, 15, 5, 'Available', NULL, 'corwason', 10, '2025-11-01'),
(5, '5', 'Cake', 'Desserts', 20, 30, 12, 'Available', NULL, 'cake', 10, '2025-11-01');

-- --------------------------------------------------------

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL,
  `prod_id` varchar(50) NOT NULL,
  `prod_name` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` double NOT NULL,
  `date` date NOT NULL,
  `em_username` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  KEY `prod_id` (`prod_id`),
  KEY `em_username` (`em_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `customer_receipt`
--

DROP TABLE IF EXISTS `customer_receipt`;
CREATE TABLE `customer_receipt` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL,
  `total` double NOT NULL,
  `date` date NOT NULL,
  `em_username` varchar(100) NOT NULL,
  `payment_method` varchar(20) DEFAULT 'cash',
  `discount` double DEFAULT 0,
  `tax` double DEFAULT 0,
  `final_amount` double NOT NULL,
  `customer_name` varchar(100) DEFAULT NULL,
  `order_status` varchar(20) DEFAULT 'completed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `customer_id` (`customer_id`),
  KEY `em_username` (`em_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cashier_stats`
--

DROP TABLE IF EXISTS `cashier_stats`;
CREATE TABLE `cashier_stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cashier_username` varchar(100) NOT NULL,
  `work_date` date NOT NULL,
  `total_orders` int(11) DEFAULT 0,
  `total_customers` int(11) DEFAULT 0,
  `total_sales` decimal(10,2) DEFAULT 0.00,
  `total_discount` decimal(10,2) DEFAULT 0.00,
  `total_tax` decimal(10,2) DEFAULT 0.00,
  `cash_sales` decimal(10,2) DEFAULT 0.00,
  `card_sales` decimal(10,2) DEFAULT 0.00,
  `cancelled_orders` int(11) DEFAULT 0,
  `refunded_orders` int(11) DEFAULT 0,
  `shift_start` timestamp NULL DEFAULT NULL,
  `shift_end` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `cashier_date` (`cashier_username`,`work_date`),
  KEY `idx_work_date` (`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `expenses`
--

DROP TABLE IF EXISTS `expenses`;
CREATE TABLE `expenses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `expense_date` date NOT NULL,
  `added_by` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `added_by` (`added_by`),
  KEY `idx_expense_date` (`expense_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `inventory_log`
--

DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `prod_id` varchar(50) NOT NULL,
  `change_type` varchar(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `performed_by` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `prod_id` (`prod_id`),
  KEY `performed_by` (`performed_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

COMMIT;
    
    
    
    
     */
}
