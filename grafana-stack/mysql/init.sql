CREATE TABLE IF NOT EXISTS metrics (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(255),
    value INT,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

INSERT INTO metrics (name, value) VALUES
                                      ('failed_login', 3),
                                      ('malware_detected', 1),
                                      ('firewall_alert', 2),
                                      ('failed_login', 5),
                                      ('malware_detected', 2);
