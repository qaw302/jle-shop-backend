CREATE TABLE IF NOT EXISTS members (
    member_id VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    birthday DATE,
    phone_number VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    authority VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL,
    tos_agreement BOOLEAN,
    privacy_agreement BOOLEAN,
    marketing_agreement BOOLEAN,
    create_at DATETIME,
    update_at DATETIME,
    PRIMARY KEY (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS withdraw_members (
    member_id VARCHAR(255) NOT NULL,
    memo VARCHAR(255),
    withdraw_at DATETIME,
    PRIMARY KEY (member_id),
    CONSTRAINT fk_withdraw_members_member
        FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS address (
    address_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255),
    alias VARCHAR(255),
    zipcode VARCHAR(255),
    default_address VARCHAR(255),
    detail_address VARCHAR(255),
    receiver_name VARCHAR(255),
    phone_number VARCHAR(255),
    is_default BOOLEAN,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (address_id),
    KEY idx_address_member_id (member_id),
    CONSTRAINT fk_address_member
        FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS delivery (
    delivery_id BIGINT NOT NULL AUTO_INCREMENT,
    delivery_code VARCHAR(255),
    delivery_company VARCHAR(255),
    shipping_fee INT,
    postal_code VARCHAR(255),
    address1 VARCHAR(255),
    address2 VARCHAR(255),
    recipient VARCHAR(255),
    phone_number VARCHAR(255),
    PRIMARY KEY (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    category_id INT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(255),
    category_level INT,
    parent_category_id INT,
    PRIMARY KEY (category_id),
    KEY idx_categories_parent_category_id (parent_category_id),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_category_id) REFERENCES categories (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS discounts (
    discount_id BIGINT NOT NULL AUTO_INCREMENT,
    discount_name VARCHAR(255),
    discount_type VARCHAR(255),
    discount_value INT,
    discount_start_date DATETIME,
    discount_end_date DATETIME,
    PRIMARY KEY (discount_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS discount_targets (
    discount_target_id BIGINT NOT NULL AUTO_INCREMENT,
    discount_id BIGINT NOT NULL,
    apply_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    PRIMARY KEY (discount_target_id),
    KEY idx_discount_targets_discount_id (discount_id),
    KEY idx_discount_targets_apply_type_target_id (apply_type, target_id),
    CONSTRAINT fk_discount_targets_discount
        FOREIGN KEY (discount_id) REFERENCES discounts (discount_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT NOT NULL AUTO_INCREMENT,
    product_name VARCHAR(255),
    product_description TEXT,
    is_deleted BOOLEAN,
    category_id INT,
    product_state VARCHAR(255),
    is_selling BOOLEAN,
    created_at DATETIME,
    price INT,
    discount_id BIGINT,
    summary_description VARCHAR(255),
    simple_description VARCHAR(255),
    thumbnail_path VARCHAR(255),
    PRIMARY KEY (product_id),
    UNIQUE KEY uk_products_product_name (product_name),
    KEY idx_products_category_id (category_id),
    KEY idx_products_discount_id (discount_id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id),
    CONSTRAINT fk_products_discount
        FOREIGN KEY (discount_id) REFERENCES discounts (discount_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_images (
    product_image_id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT,
    path VARCHAR(255) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    status VARCHAR(10) NOT NULL,
    PRIMARY KEY (product_image_id),
    KEY idx_product_images_product_id (product_id),
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS option_type (
    option_type_id BIGINT NOT NULL AUTO_INCREMENT,
    option_type_name VARCHAR(255),
    created_at DATETIME,
    PRIMARY KEY (option_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS option_value (
    option_value_id BIGINT NOT NULL AUTO_INCREMENT,
    option_value_name VARCHAR(255),
    option_type_id BIGINT,
    PRIMARY KEY (option_value_id),
    KEY idx_option_value_option_type_id (option_type_id),
    CONSTRAINT fk_option_value_option_type
        FOREIGN KEY (option_type_id) REFERENCES option_type (option_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_option (
    product_option_id BIGINT NOT NULL AUTO_INCREMENT,
    product_option_name VARCHAR(255),
    created_at DATETIME,
    stock_quantity INT,
    additional_price INT,
    product_id BIGINT,
    PRIMARY KEY (product_option_id),
    KEY idx_product_option_product_id (product_id),
    CONSTRAINT fk_product_option_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_option_detail (
    product_option_detail_id BIGINT NOT NULL AUTO_INCREMENT,
    product_option_detail_name VARCHAR(255),
    product_option_type VARCHAR(255),
    product_option_id BIGINT,
    PRIMARY KEY (product_option_detail_id),
    KEY idx_product_option_detail_product_option_id (product_option_id),
    CONSTRAINT fk_product_option_detail_product_option
        FOREIGN KEY (product_option_id) REFERENCES product_option (product_option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255),
    product_option_id BIGINT,
    quantity INT,
    PRIMARY KEY (cart_item_id),
    UNIQUE KEY uq_cart_member_option (member_id, product_option_id),
    KEY idx_cart_items_member_id (member_id),
    KEY idx_cart_items_product_option_id (product_option_id),
    CONSTRAINT fk_cart_items_member
        FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_cart_items_product_option
        FOREIGN KEY (product_option_id) REFERENCES product_option (product_option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags (
    tag_id BIGINT NOT NULL AUTO_INCREMENT,
    tag_name VARCHAR(255),
    PRIMARY KEY (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT,
    tag_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_product_tags_product_id (product_id),
    KEY idx_product_tags_tag_id (tag_id),
    CONSTRAINT fk_product_tags_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT fk_product_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_category_mappings (
    product_category_mapping_id INT NOT NULL AUTO_INCREMENT,
    category_id INT,
    product_id BIGINT,
    PRIMARY KEY (product_category_mapping_id),
    KEY idx_product_category_mappings_category_id (category_id),
    KEY idx_product_category_mappings_product_id (product_id),
    CONSTRAINT fk_product_category_mappings_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id),
    CONSTRAINT fk_product_category_mappings_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coupons (
    coupon_id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_code VARCHAR(255) NOT NULL,
    issue_type VARCHAR(255) NOT NULL,
    coupon_name VARCHAR(255) NOT NULL,
    coupon_type VARCHAR(255) NOT NULL,
    discount_amount INT NOT NULL,
    coupon_start_at DATETIME,
    coupon_end_at DATETIME,
    min_order_price INT,
    max_discount_price INT,
    commnet VARCHAR(255),
    created_at DATETIME,
    deleted_at DATETIME,
    PRIMARY KEY (coupon_id),
    UNIQUE KEY uk_coupon_code (coupon_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS member_coupons (
    member_coupon_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255) NOT NULL,
    coupon_id BIGINT NOT NULL,
    issued_at DATETIME,
    expired_at DATETIME,
    status VARCHAR(255),
    PRIMARY KEY (member_coupon_id),
    UNIQUE KEY uk_member_coupons_member_coupon (member_id, coupon_id),
    KEY idx_member_coupons_member_id (member_id),
    KEY idx_member_coupons_coupon_id (coupon_id),
    CONSTRAINT fk_member_coupons_member
        FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_member_coupons_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coupon_histories (
    coupon_history_id BIGINT NOT NULL AUTO_INCREMENT,
    member_coupon_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (coupon_history_id),
    KEY idx_coupon_histories_member_coupon_id (member_coupon_id),
    CONSTRAINT fk_coupon_histories_member_coupon
        FOREIGN KEY (member_coupon_id) REFERENCES member_coupons (member_coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(255) NOT NULL,
    member_id VARCHAR(255),
    order_date DATETIME,
    total_price INT,
    order_status VARCHAR(255),
    order_title VARCHAR(255),
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_orders_order_number (order_number),
    KEY idx_orders_member_id (member_id),
    CONSTRAINT fk_orders_member
        FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_benefits (
    order_id BIGINT NOT NULL,
    point_discount INT,
    coupon_discount INT,
    member_coupon_id BIGINT,
    total_benefit BIGINT,
    product_original_total_price INT,
    product_total_discount_amount INT,
    PRIMARY KEY (order_id),
    KEY idx_order_benefits_member_coupon_id (member_coupon_id),
    CONSTRAINT fk_order_benefits_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_order_benefits_member_coupon
        FOREIGN KEY (member_coupon_id) REFERENCES member_coupons (member_coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_products (
    order_product_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT,
    delivery_id BIGINT,
    quantity INT,
    order_price INT,
    order_product_status VARCHAR(255),
    product_id BIGINT,
    product_option_id BIGINT,
    product_option_name VARCHAR(255),
    product_name VARCHAR(255),
    discount_id BIGINT,
    discount_type TINYINT,
    disocunt_value INT,
    original_price INT,
    PRIMARY KEY (order_product_id),
    KEY idx_order_products_order_id (order_id),
    KEY idx_order_products_delivery_id (delivery_id),
    CONSTRAINT fk_order_products_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_order_products_delivery
        FOREIGN KEY (delivery_id) REFERENCES delivery (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
