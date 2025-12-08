erDiagram
    employee ||--o{ customer : "creates"
    employee ||--o{ customer_receipt : "processes"
    employee ||--o{ cashier_stats : "has"
    employee ||--o{ expenses : "adds"
    employee ||--o{ inventory_log : "performs"
    
    product ||--o{ customer : "ordered_in"
    product ||--o{ inventory_log : "logged_in"
    
    customer_receipt ||--|| customer : "contains"
    
    employee {
        int id PK
        varchar username UK
        varchar password
        varchar question
        varchar answer
        varchar role "admin/cashier"
        varchar full_name
        varchar phone
        varchar email
        decimal salary
        date hire_date
        varchar status "active/inactive"
        date date
        timestamp last_login
    }
    
    product {
        int id PK
        varchar prod_id UK
        varchar prod_name
        varchar type "Drinks/Food/Desserts"
        int stock
        double price
        double cost
        varchar status "Available/Unavailable"
        varchar image
        text description
        int min_stock_level
        date date
    }
    
    customer {
        int id PK
        int customer_id FK
        varchar prod_id FK
        varchar prod_name
        varchar type
        int quantity
        double price
        date date
        varchar em_username FK
    }
    
    customer_receipt {
        int id PK
        int customer_id UK
        double total
        date date
        varchar em_username FK
        varchar payment_method "cash/card"
        double discount
        double tax
        double final_amount
        varchar customer_name
        varchar order_status "completed/cancelled/refunded"
    }
    
    cashier_stats {
        int id PK
        varchar cashier_username FK
        date work_date
        int total_orders
        int total_customers
        decimal total_sales
        decimal total_discount
        decimal total_tax
        decimal cash_sales
        decimal card_sales
        int cancelled_orders
        int refunded_orders
        timestamp shift_start
        timestamp shift_end
        timestamp created_at
    }
    
    expenses {
        int id PK
        varchar category
        text description
        decimal amount
        date expense_date
        varchar added_by FK
        timestamp created_at
    }
    
    inventory_log {
        int id PK
        varchar prod_id FK
        varchar change_type "add/remove/adjust"
        int quantity
        varchar reason
        varchar performed_by FK
        timestamp created_at
    }
