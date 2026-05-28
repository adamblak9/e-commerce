/*
  ShopEase demo data for SQL Server.

  Accounts:
    admin@gmail.com / admin123
    customers use / customer123
    sellers use / seller123

  Run this against ecommerce_db after Hibernate has created/updated the tables.
*/

BEGIN TRANSACTION;

DELETE FROM reviews WHERE id BETWEEN 40001 AND 40200;
DELETE FROM order_items WHERE id BETWEEN 30001 AND 30200;
DELETE FROM orders WHERE id BETWEEN 20001 AND 20050;
DELETE FROM products WHERE id BETWEEN 10001 AND 10050;
DELETE FROM categories WHERE id BETWEEN 8001 AND 8010;
DELETE FROM users WHERE id BETWEEN 9001 AND 9011 OR email IN (
    'admin@gmail.com',
    'amina.customer@gmail.com',
    'youssef.customer@gmail.com',
    'lina.customer@gmail.com',
    'nora.seller@gmail.com',
    'samir.seller@gmail.com',
    'mira.seller@gmail.com',
    'sara@gmail.com',
    'omar@gmail.com',
    'hajar@gmail.com',
    'adam@gmail.com'
);

SET IDENTITY_INSERT users ON;
INSERT INTO users (id, username, email, password_hash, first_name, last_name, phone_number, role, status, created_at, updated_at, last_login_at) VALUES
(9001, 'admin', 'admin@gmail.com', '$2a$12$tuY719hfU35Xhr7jY3XutONkMWXpPUGQlOfZSYLFNhwUzaa1BEXT.', 'Admin', 'ShopEase', '+212600000001', 'ADMIN', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9002, 'amina_benali', 'amina.customer@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Amina', 'Benali', '+212600000002', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9003, 'youssef_alaoui', 'youssef.customer@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Youssef', 'Alaoui', '+212600000003', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9004, 'lina_haddad', 'lina.customer@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Lina', 'Haddad', '+212600000004', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9005, 'nora_tech', 'nora.seller@gmail.com', '$2a$12$SnOxUZ0EYprHlN/2q01X6e903gW0e4no0JISrM4B98g8sRyDKMRl2', 'Nora', 'Tech', '+212600000005', 'SELLER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9006, 'samir_home', 'samir.seller@gmail.com', '$2a$12$SnOxUZ0EYprHlN/2q01X6e903gW0e4no0JISrM4B98g8sRyDKMRl2', 'Samir', 'Home', '+212600000006', 'SELLER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9007, 'mira_style', 'mira.seller@gmail.com', '$2a$12$SnOxUZ0EYprHlN/2q01X6e903gW0e4no0JISrM4B98g8sRyDKMRl2', 'Mira', 'Style', '+212600000007', 'SELLER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9008, 'sara_k', 'sara@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Sara', 'Karim', '+212600000008', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9009, 'omar_b', 'omar@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Omar', 'Bennani', '+212600000009', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9010, 'hajar_t', 'hajar@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Hajar', 'Tahiri', '+212600000010', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL),
(9011, 'adam_r', 'adam@gmail.com', '$2a$12$BziwuGfAcel3aTwMi100eu1Sp58fShTcuacZc8oYmbuBmKPNYYmZ2', 'Adam', 'Rami', '+212600000011', 'CUSTOMER', 'ACTIVE', SYSDATETIME(), SYSDATETIME(), NULL)
;
SET IDENTITY_INSERT users OFF;

SET IDENTITY_INSERT categories ON;
INSERT INTO categories (id, name, description, slug, icon, active, created_at, updated_at) VALUES
(8001, 'Electronics', 'Phones, audio, gaming, and desk tech.', 'electronics', 'E', 1, SYSDATETIME(), SYSDATETIME()),
(8002, 'Home & Living', 'Comfort, lighting, storage, and kitchen goods.', 'home-living', 'H', 1, SYSDATETIME(), SYSDATETIME()),
(8003, 'Fashion', 'Wardrobe essentials and daily accessories.', 'fashion', 'F', 1, SYSDATETIME(), SYSDATETIME()),
(8004, 'Beauty', 'Care, grooming, and wellness products.', 'beauty', 'B', 1, SYSDATETIME(), SYSDATETIME()),
(8005, 'Sports', 'Fitness, recovery, and outdoor essentials.', 'sports', 'S', 1, SYSDATETIME(), SYSDATETIME());
SET IDENTITY_INSERT categories OFF;

SET IDENTITY_INSERT products ON;
INSERT INTO products (id, name, description, price, stock_quantity, category, image_url, seller_id, status, created_at, updated_at) VALUES
(10001, 'Nova X Wireless Headphones', 'Noise reducing wireless headphones with long battery life.', 89.99, 42, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-22,SYSDATETIME()), SYSDATETIME()),
(10002, 'Arc Mini Bluetooth Speaker', 'Compact speaker with punchy bass for desk or travel.', 39.50, 60, 'Electronics', 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-18,SYSDATETIME()), SYSDATETIME()),
(10003, 'PulseFit Smart Watch', 'Fitness watch with heart tracking and notification sync.', 129.00, 28, 'Electronics', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-12,SYSDATETIME()), SYSDATETIME()),
(10004, 'TypePro Mechanical Keyboard', 'Compact keyboard with tactile switches and backlight.', 74.90, 35, 'Electronics', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-9,SYSDATETIME()), SYSDATETIME()),
(10005, 'Glide Wireless Mouse', 'Ergonomic wireless mouse for work and gaming.', 29.99, 75, 'Electronics', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-7,SYSDATETIME()), SYSDATETIME()),
(10006, 'Slim USB-C Hub', 'Seven port aluminum hub for laptops and tablets.', 44.00, 50, 'Electronics', 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-16,SYSDATETIME()), SYSDATETIME()),
(10007, 'CloudCharge Power Bank', 'Fast charging 20000mAh power bank.', 49.99, 33, 'Electronics', 'https://images.unsplash.com/photo-1609592806596-b43bada2f95b?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-11,SYSDATETIME()), SYSDATETIME()),
(10008, 'Luma Desk Lamp', 'Adjustable LED lamp with warm and cool modes.', 34.99, 41, 'Home & Living', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-20,SYSDATETIME()), SYSDATETIME()),
(10009, 'Nordic Ceramic Mug Set', 'Four matte ceramic mugs for daily coffee.', 24.50, 88, 'Home & Living', 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-30,SYSDATETIME()), SYSDATETIME()),
(10010, 'SoftWeave Throw Blanket', 'Warm woven throw for sofa or bed styling.', 54.00, 26, 'Home & Living', 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-14,SYSDATETIME()), SYSDATETIME()),
(10011, 'Bamboo Storage Baskets', 'Nested baskets for shelves, laundry, and toys.', 32.00, 39, 'Home & Living', 'https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-19,SYSDATETIME()), SYSDATETIME()),
(10012, 'Aroma Ceramic Diffuser', 'Quiet ultrasonic diffuser with soft night light.', 46.75, 31, 'Home & Living', 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-6,SYSDATETIME()), SYSDATETIME()),
(10013, 'Everyday Cotton Hoodie', 'Soft fleece hoodie with relaxed fit.', 47.99, 55, 'Fashion', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-8,SYSDATETIME()), SYSDATETIME()),
(10014, 'Canvas Weekender Tote', 'Durable tote bag with inner pockets.', 38.00, 44, 'Fashion', 'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-21,SYSDATETIME()), SYSDATETIME()),
(10015, 'Minimal White Sneakers', 'Clean daily sneakers with cushioned sole.', 69.99, 37, 'Fashion', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-25,SYSDATETIME()), SYSDATETIME()),
(10016, 'Classic Denim Jacket', 'Medium wash denim jacket with metal buttons.', 78.50, 20, 'Fashion', 'https://images.unsplash.com/photo-1543076447-215ad9ba6923?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-13,SYSDATETIME()), SYSDATETIME()),
(10017, 'Leather Card Wallet', 'Slim wallet with six card slots.', 22.99, 95, 'Fashion', 'https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-5,SYSDATETIME()), SYSDATETIME()),
(10018, 'HydraGlow Face Serum', 'Lightweight hydrating serum for daily routine.', 26.90, 62, 'Beauty', 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-10,SYSDATETIME()), SYSDATETIME()),
(10019, 'Matte Grooming Kit', 'Travel grooming kit with compact accessories.', 31.00, 40, 'Beauty', 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-17,SYSDATETIME()), SYSDATETIME()),
(10020, 'Calm Bath Salt Jar', 'Mineral bath salts with relaxing scent.', 18.25, 70, 'Beauty', 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-26,SYSDATETIME()), SYSDATETIME()),
(10021, 'FlexRun Training Shoes', 'Light training shoes for gym and running.', 84.00, 30, 'Sports', 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-15,SYSDATETIME()), SYSDATETIME()),
(10022, 'Grip Yoga Mat', 'Non slip yoga mat with carry strap.', 27.50, 58, 'Sports', 'https://images.unsplash.com/photo-1592432678016-e910b452f9a2?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-4,SYSDATETIME()), SYSDATETIME()),
(10023, 'Steel Water Bottle', 'Insulated bottle keeps drinks cold all day.', 21.99, 82, 'Sports', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-3,SYSDATETIME()), SYSDATETIME()),
(10024, 'Adjustable Dumbbell Pair', 'Space saving dumbbells for home workouts.', 119.00, 16, 'Sports', 'https://images.unsplash.com/photo-1586401100295-7a8096fd231a?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-27,SYSDATETIME()), SYSDATETIME()),
(10025, 'Trail Day Backpack', 'Lightweight backpack with bottle pockets.', 59.90, 25, 'Sports', 'https://images.unsplash.com/photo-1622560480654-d96214fdc887?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-2,SYSDATETIME()), SYSDATETIME()),
(10026, 'Studio Monitor Stand', 'Raised desk stand for monitor and laptop.', 36.00, 45, 'Electronics', 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-28,SYSDATETIME()), SYSDATETIME()),
(10027, 'HD Webcam Pro', 'Full HD webcam with privacy cover.', 52.99, 29, 'Electronics', 'https://images.unsplash.com/photo-1587614295999-6c1c1367511e?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-1,SYSDATETIME()), SYSDATETIME()),
(10028, 'Wireless Charging Pad', 'Slim Qi charging pad for phones and earbuds.', 19.99, 66, 'Electronics', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-6,SYSDATETIME()), SYSDATETIME()),
(10029, 'Kitchen Scale Digital', 'Precise digital scale for cooking and baking.', 17.99, 49, 'Home & Living', 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-12,SYSDATETIME()), SYSDATETIME()),
(10030, 'Glass Food Containers', 'Stackable airtight containers for meal prep.', 42.00, 34, 'Home & Living', 'https://images.unsplash.com/photo-1593182440959-9d5165b29b88?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-18,SYSDATETIME()), SYSDATETIME()),
(10031, 'Linen Pillow Covers', 'Set of two soft linen pillow covers.', 28.00, 53, 'Home & Living', 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-22,SYSDATETIME()), SYSDATETIME()),
(10032, 'Everyday Oxford Shirt', 'Breathable button shirt for work or weekend.', 45.00, 48, 'Fashion', 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-11,SYSDATETIME()), SYSDATETIME()),
(10033, 'Ribbed Knit Beanie', 'Warm ribbed beanie in neutral color.', 16.50, 73, 'Fashion', 'https://images.unsplash.com/photo-1576871337622-98d48d1cf531?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-16,SYSDATETIME()), SYSDATETIME()),
(10034, 'Polarized Sunglasses', 'Lightweight sunglasses with UV protection.', 24.99, 52, 'Fashion', 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-29,SYSDATETIME()), SYSDATETIME()),
(10035, 'Daily Cleanser Gel', 'Gentle facial cleanser for morning and night.', 15.99, 80, 'Beauty', 'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-7,SYSDATETIME()), SYSDATETIME()),
(10036, 'Vitamin C Moisturizer', 'Brightening moisturizer with soft finish.', 21.50, 67, 'Beauty', 'https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-13,SYSDATETIME()), SYSDATETIME()),
(10037, 'Recovery Foam Roller', 'Firm roller for stretching and recovery.', 23.75, 46, 'Sports', 'https://images.unsplash.com/photo-1605296867304-46d5465a13f1?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-23,SYSDATETIME()), SYSDATETIME()),
(10038, 'Resistance Band Set', 'Five resistance bands with carry pouch.', 18.99, 90, 'Sports', 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-8,SYSDATETIME()), SYSDATETIME()),
(10039, 'Gaming Controller', 'Wireless controller for PC and console play.', 58.00, 27, 'Electronics', 'https://images.unsplash.com/photo-1592840496694-26d035b52b48?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-5,SYSDATETIME()), SYSDATETIME()),
(10040, 'Portable SSD 1TB', 'Fast external storage in pocket size.', 99.00, 22, 'Electronics', 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=1200&q=90', 9005, 'ACTIVE', DATEADD(day,-19,SYSDATETIME()), SYSDATETIME()),
(10041, 'Minimal Wall Clock', 'Quiet wall clock with clean face.', 33.00, 38, 'Home & Living', 'https://images.unsplash.com/photo-1563861826100-9cb868fdbe1c?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-10,SYSDATETIME()), SYSDATETIME()),
(10042, 'Desk Cable Tray', 'Under desk cable tray for cleaner setups.', 20.00, 64, 'Home & Living', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-6,SYSDATETIME()), SYSDATETIME()),
(10043, 'Structured Crossbody Bag', 'Compact crossbody bag with adjustable strap.', 52.00, 32, 'Fashion', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-4,SYSDATETIME()), SYSDATETIME()),
(10044, 'Soft Crew Socks Pack', 'Five pair cotton sock pack.', 14.00, 110, 'Fashion', 'https://images.unsplash.com/photo-1586350977771-b3b0abd50c82?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-15,SYSDATETIME()), SYSDATETIME()),
(10045, 'Hair Care Repair Mask', 'Rich repair mask for dry hair.', 19.99, 57, 'Beauty', 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-20,SYSDATETIME()), SYSDATETIME()),
(10046, 'Beard Oil Cedar', 'Soft cedar beard oil with natural finish.', 13.99, 72, 'Beauty', 'https://images.unsplash.com/photo-1621607512214-68297480165e?auto=format&fit=crop&w=1200&q=90', 9007, 'ACTIVE', DATEADD(day,-9,SYSDATETIME()), SYSDATETIME()),
(10047, 'Jump Rope Speed Pro', 'Adjustable speed rope for cardio training.', 12.50, 85, 'Sports', 'https://images.unsplash.com/photo-1599058917212-d750089bc07e?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-2,SYSDATETIME()), SYSDATETIME()),
(10048, 'Cycling Bottle Cage', 'Light aluminum bottle cage for bikes.', 11.90, 76, 'Sports', 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-24,SYSDATETIME()), SYSDATETIME()),
(10049, 'Noise Cancel Earbuds', 'Small wireless earbuds with active noise canceling.', 79.99, 0, 'Electronics', 'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=1200&q=90', 9005, 'OUT_OF_STOCK', DATEADD(day,-31,SYSDATETIME()), SYSDATETIME()),
(10050, 'Premium Espresso Maker', 'Compact espresso maker for home coffee corners.', 149.00, 12, 'Home & Living', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=90', 9006, 'ACTIVE', DATEADD(day,-3,SYSDATETIME()), SYSDATETIME());
SET IDENTITY_INSERT products OFF;

SET IDENTITY_INSERT orders ON;
INSERT INTO orders (id, user_id, status, total_amount, shipping_address, created_at, updated_at, delivered_at) VALUES
(20001, 9002, 'DELIVERED', 159.98, '12 Market Street, Casablanca', DATEADD(day,-12,SYSDATETIME()), DATEADD(day,-9,SYSDATETIME()), DATEADD(day,-9,SYSDATETIME())),
(20002, 9003, 'SHIPPED', 147.99, '45 Avenue Hassan II, Rabat', DATEADD(day,-6,SYSDATETIME()), DATEADD(day,-3,SYSDATETIME()), NULL),
(20003, 9004, 'CONFIRMED', 211.49, '8 Palm Residence, Marrakech', DATEADD(day,-3,SYSDATETIME()), DATEADD(day,-2,SYSDATETIME()), NULL),
(20004, 9002, 'PENDING', 65.74, '12 Market Street, Casablanca', DATEADD(day,-1,SYSDATETIME()), DATEADD(day,-1,SYSDATETIME()), NULL),
(20005, 9003, 'DELIVERED', 186.98, '45 Avenue Hassan II, Rabat', DATEADD(day,-18,SYSDATETIME()), DATEADD(day,-15,SYSDATETIME()), DATEADD(day,-15,SYSDATETIME()));
SET IDENTITY_INSERT orders OFF;

SET IDENTITY_INSERT order_items ON;
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, price, image_url) VALUES
(30001, 20001, 10001, 'Nova X Wireless Headphones', 1, 89.99, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=90'),
(30002, 20001, 10015, 'Minimal White Sneakers', 1, 69.99, 'https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=1200&q=90'),
(30003, 20002, 10008, 'Luma Desk Lamp', 1, 34.99, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=1200&q=90'),
(30004, 20002, 10040, 'Portable SSD 1TB', 1, 99.00, 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=1200&q=90'),
(30005, 20002, 10044, 'Soft Crew Socks Pack', 1, 14.00, 'https://images.unsplash.com/photo-1586350977771-b3b0abd50c82?auto=format&fit=crop&w=1200&q=90'),
(30006, 20003, 10024, 'Adjustable Dumbbell Pair', 1, 119.00, 'https://images.unsplash.com/photo-1586401100295-7a8096fd231a?auto=format&fit=crop&w=1200&q=90'),
(30007, 20003, 10016, 'Classic Denim Jacket', 1, 78.50, 'https://images.unsplash.com/photo-1543076447-215ad9ba6923?auto=format&fit=crop&w=1200&q=90'),
(30008, 20003, 10046, 'Beard Oil Cedar', 1, 13.99, 'https://images.unsplash.com/photo-1621607512214-68297480165e?auto=format&fit=crop&w=1200&q=90'),
(30009, 20004, 10022, 'Grip Yoga Mat', 1, 27.50, 'https://images.unsplash.com/photo-1592432678016-e910b452f9a2?auto=format&fit=crop&w=1200&q=90'),
(30010, 20004, 10028, 'Wireless Charging Pad', 1, 19.99, 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=1200&q=90'),
(30011, 20004, 10020, 'Calm Bath Salt Jar', 1, 18.25, 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?auto=format&fit=crop&w=1200&q=90'),
(30012, 20005, 10050, 'Premium Espresso Maker', 1, 149.00, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=90'),
(30013, 20005, 10023, 'Steel Water Bottle', 1, 21.99, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=1200&q=90'),
(30014, 20005, 10035, 'Daily Cleanser Gel', 1, 15.99, 'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=90');
SET IDENTITY_INSERT order_items OFF;

SET IDENTITY_INSERT reviews ON;
INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at, updated_at) VALUES
(40001, 10001, 9002, 5, 'Great sound and the battery lasts through my work day.', DATEADD(day,-10,SYSDATETIME()), SYSDATETIME()),
(40002, 10015, 9002, 4, 'Clean design and comfortable after a few hours.', DATEADD(day,-10,SYSDATETIME()), SYSDATETIME()),
(40003, 10008, 9003, 5, 'The warm light mode is perfect for my desk.', DATEADD(day,-5,SYSDATETIME()), SYSDATETIME()),
(40004, 10040, 9003, 4, 'Fast drive, small enough to keep in my bag.', DATEADD(day,-5,SYSDATETIME()), SYSDATETIME()),
(40005, 10024, 9004, 5, 'Solid quality and saves a lot of space at home.', DATEADD(day,-2,SYSDATETIME()), SYSDATETIME()),
(40006, 10016, 9004, 4, 'Nice fit and looks exactly like the product image.', DATEADD(day,-2,SYSDATETIME()), SYSDATETIME()),
(40007, 10050, 9003, 5, 'Makes strong coffee and looks good on the counter.', DATEADD(day,-14,SYSDATETIME()), SYSDATETIME()),
(40008, 10023, 9002, 5, 'Keeps water cold for hours.', DATEADD(day,-1,SYSDATETIME()), SYSDATETIME()),
(40009, 10035, 9004, 4, 'Gentle cleanser, no strong smell.', DATEADD(day,-4,SYSDATETIME()), SYSDATETIME()),
(40010, 10022, 9002, 4, 'Good grip for daily stretching.', DATEADD(day,-1,SYSDATETIME()), SYSDATETIME());
SET IDENTITY_INSERT reviews OFF;


-- Additional professional demo data
SET IDENTITY_INSERT orders ON;
INSERT INTO orders (id, user_id, status, total_amount, shipping_address, created_at, updated_at, delivered_at) VALUES
(20006, 9008, 'DELIVERED', 220.50, '101 Maarif, Casablanca', DATEADD(day,-7,SYSDATETIME()), DATEADD(day,-5,SYSDATETIME()), DATEADD(day,-5,SYSDATETIME())),
(20007, 9009, 'PENDING', 89.99, '33 Agdal, Rabat', DATEADD(day,-1,SYSDATETIME()), DATEADD(day,-1,SYSDATETIME()), NULL),
(20008, 9010, 'SHIPPED', 150.00, '77 Marina, Agadir', DATEADD(day,-2,SYSDATETIME()), DATEADD(day,-1,SYSDATETIME()), NULL);
SET IDENTITY_INSERT orders OFF;

SET IDENTITY_INSERT reviews ON;
INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at, updated_at) VALUES
(40011, 10001, 9008, 5, 'Amazing sound quality. Worth every dirham.', SYSDATETIME(), SYSDATETIME()),
(40012, 10001, 9009, 4, 'Battery is great but packaging could improve.', SYSDATETIME(), SYSDATETIME()),
(40013, 10015, 9010, 5, 'Very stylish and super comfortable.', SYSDATETIME(), SYSDATETIME()),
(40014, 10015, 9011, 3, 'Looks good but runs slightly small.', SYSDATETIME(), SYSDATETIME()),
(40015, 10024, 9008, 5, 'Gym quality at home. Excellent.', SYSDATETIME(), SYSDATETIME()),
(40016, 10050, 9009, 5, 'Coffee tastes fantastic. Love it.', SYSDATETIME(), SYSDATETIME()),
(40017, 10028, 9010, 4, 'Fast charging works perfectly.', SYSDATETIME(), SYSDATETIME()),
(40018, 10040, 9011, 5, 'Blazing fast transfer speed.', SYSDATETIME(), SYSDATETIME());
SET IDENTITY_INSERT reviews OFF;

UPDATE products SET stock_quantity = 3 WHERE id = 10001;
UPDATE products SET stock_quantity = 5 WHERE id = 10015;
UPDATE products SET stock_quantity = 2 WHERE id = 10050;


COMMIT TRANSACTION;
