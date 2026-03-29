MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('TALLINN', 'CAR', 4.0);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('TALLINN', 'SCOOTER', 3.5);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('TALLINN', 'BIKE', 3.0);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('TARTU', 'CAR', 3.5);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('TARTU', 'SCOOTER', 3.0);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('TARTU', 'BIKE', 2.5);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('PÄRNU', 'CAR', 3.0);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('PÄRNU', 'SCOOTER', 2.5);
MERGE INTO base_fees (city, vehicle_type, fee) KEY (city, vehicle_type) VALUES ('PÄRNU', 'BIKE', 2.0);

MERGE INTO city_station_mapping (city, station_name) KEY (city) VALUES ('TALLINN', 'Tallinn-Harku');
MERGE INTO city_station_mapping (city, station_name) KEY (city) VALUES ('TARTU', 'Tartu-Tõravere');
MERGE INTO city_station_mapping (city, station_name) KEY (city) VALUES ('PÄRNU', 'Pärnu');