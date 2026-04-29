-- phpMyAdmin SQL Dump - Fixed Version
-- Database: `cafe`
CREATE DATABASE IF NOT EXISTS cafe;
USE cafe;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- --------------------------------------------------------

--
-- Table structure for table `customer_receipt` - FIXED
--

DROP TABLE IF EXISTS `customer_receipt`;

CREATE TABLE `customer_receipt` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL,
  `customer_name` varchar(100) DEFAULT 'Walk-in Customer',
  `order_type` varchar(20) DEFAULT 'Dine In',
  `payment_method` varchar(20) DEFAULT 'Cash',
  `total` double NOT NULL DEFAULT 0,
  `discount` double DEFAULT 0,
  `tax` double DEFAULT 0,
  `final_amount` double NOT NULL,
  `order_status` varchar(20) DEFAULT 'completed',
  `date` date NOT NULL,
  `em_username` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  KEY `em_username` (`em_username`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

--
-- Dumping data for table `employee`
--

INSERT INTO `employee` (`id`, `username`, `password`, `question`, `answer`, `role`, `full_name`, `phone`, `email`, `salary`, `hire_date`, `status`, `date`, `last_login`) VALUES
(1, 'admin', 'e66b62381da494378a3871c65ab56bf1aef054c4a1b6c8cc57f597622d99721b', 'What is your favorite color?', 'Blue', 'admin', 'leader', '0123456789', 'admin@cafe.com', NULL, NULL, 'active', NULL, '2025-12-04 16:26:26'),
(3, 'cashier1', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', 'What is your favorite color?', 'green', 'cashier', 'testcashier', '0111111111', 'test@gmail.com', 3000.00, '2025-10-27', 'active', '2025-10-27', '2025-12-04 16:32:02');

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;

CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` varchar(50) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `price` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product`
--

INSERT INTO `product` (`id`, `product_id`, `product_name`, `type`, `stock`, `price`, `status`, `image`, `date`) VALUES
(1, '1', 'Espresso', 'Drinks', 50, 15.00, 'Available', '', '2025-11-01'),
(2, '2', 'Cappuccino', 'Drinks', 45, 20.00, 'Available', '', '2025-11-01'),
(3, '3', 'Latte', 'Drinks', 40, 22.00, 'Available', '', '2025-11-01'),
(4, '4', 'Croissant', 'Meals', 30, 15.00, 'Available', '', '2025-11-01'),
(5, '5', 'Cake', 'Desserts', 20, 30.00, 'Available', '', '2025-11-01');

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;

CREATE TABLE `order_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `receipt_id` int(11) NOT NULL,
  `product_id` varchar(50) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` double NOT NULL,
  `total` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `receipt_id` (`receipt_id`),
  KEY `product_id` (`product_id`)
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