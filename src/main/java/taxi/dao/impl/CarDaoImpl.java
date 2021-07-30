package taxi.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import taxi.dao.CarDao;
import taxi.exception.DataProcessingException;
import taxi.lib.Dao;
import taxi.model.Car;
import taxi.model.Driver;
import taxi.model.Manufacturer;
import taxi.util.ConnectionUtil;

@Dao
public class CarDaoImpl implements CarDao {
    private static final int PARAMETER_SHIFT = 2;

    @Override
    public Car create(Car car) {
        String createQuery = "INSERT INTO cars (model, manufacturer_id) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement createCarStatement = connection.prepareStatement(createQuery,
                        Statement.RETURN_GENERATED_KEYS)) {
            createCarStatement.setString(1, car.getModel());
            createCarStatement.setLong(2, car.getManufacturer().getId());
            createCarStatement.executeUpdate();
            ResultSet resultSet = createCarStatement.getGeneratedKeys();
            if (resultSet.next()) {
                car.setId(resultSet.getObject(1, Long.class));
            }
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't create " + car, throwable);
        }
        createConnectionBetweenCarAndDriver(car);
        return car;
    }

    private void createConnectionBetweenCarAndDriver(Car car) {
        Long carId = car.getId();
        List<Driver> drivers = car.getDrivers();
        if (drivers.size() == 0) {
            return;
        }
        String insertQuery = "INSERT INTO cars_drivers (car_id, driver_id) VALUES "
                + drivers.stream().map(driver -> "(?, ?)").collect(Collectors.joining(", "))
                + " ON DUPLICATE KEY UPDATE car_id = car_id";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(insertQuery)) {
            for (int i = 0; i < drivers.size(); i++) {
                Driver driver = drivers.get(i);
                preparedStatement.setLong((i * PARAMETER_SHIFT) + 1, carId);
                preparedStatement.setLong((i * PARAMETER_SHIFT) + 2, driver.getId());
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataProcessingException("Can't insert drivers " + drivers, e);
        }
    }

    @Override
    public Optional<Car> get(Long id) {
        String getQuery = "SELECT c.id, model, manufacturer_id, name, country FROM cars c "
                + "JOIN manufacturers m ON c.manufacturer_id=m.id "
                + "WHERE c.deleted=false AND c.id=?;";
        Car car = null;
        try (Connection connection = ConnectionUtil.getConnection();
                 PreparedStatement getStatement = connection.prepareStatement(getQuery)) {
            getStatement.setLong(1, id);
            ResultSet resultSet = getStatement.executeQuery();
            if (resultSet.next()) {
                car = createCarWithoutDrivers(resultSet);
            }
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't get car", throwable);
        }
        if (car != null) {
            car.setDrivers(getDriversForCar(id));
        }
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> getAll() {
        String getAllQuery = "SELECT c.id, model, manufacturer_id, name, country FROM cars c "
                + "JOIN manufacturers m ON c.manufacturer_id=m.id "
                + "WHERE c.deleted=false;";
        List<Car> cars;
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getAllStatement = connection.prepareStatement(getAllQuery)) {
            ResultSet resultSet = getAllStatement.executeQuery();
            cars = new ArrayList<>();
            while (resultSet.next()) {
                cars.add(createCarWithoutDrivers(resultSet));
            }
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't get list of cars", throwable);
        }
        for (Car car : cars) {
            car.setDrivers(getDriversForCar(car.getId()));
        }
        return cars;
    }

    private Car createCarWithoutDrivers(ResultSet resultSet) throws SQLException {
        Long manufacturerId = resultSet.getObject("manufacturer_id", Long.class);
        String manufacturerName = resultSet.getString("name");
        String manufacturerCountry = resultSet.getString("country");
        Manufacturer manufacturer =
                new Manufacturer(manufacturerId, manufacturerName, manufacturerCountry);
        Long carId = resultSet.getObject("id", Long.class);
        String model = resultSet.getString("model");
        return new Car(carId, model, manufacturer);
    }

    private List<Driver> getDriversForCar(Long carId) {
        String getDriversQuery = "SELECT id, name, license_number FROM drivers d "
                + "JOIN cars_drivers cd ON d.id=cd.driver_id "
                + "WHERE deleted=false AND cd.car_id=?;";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getDriverStatement =
                        connection.prepareStatement(getDriversQuery)) {
            getDriverStatement.setLong(1, carId);
            ResultSet resultSet = getDriverStatement.executeQuery();
            List<Driver> drivers = new ArrayList<>();
            while (resultSet.next()) {
                drivers.add(parseDriverFromResultSet(resultSet));
            }
            return drivers;
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't get list of drivers", throwable);
        }
    }

    private Driver parseDriverFromResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String licenseNumber = resultSet.getString("license_number");
        return new Driver(id, name, licenseNumber);
    }

    @Override
    public Car update(Car car) {
        String updateQuery = "UPDATE cars SET model=?, manufacturer_id=? "
                + "WHERE id=? AND deleted=false;";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setString(1, car.getModel());
            updateStatement.setLong(2, car.getManufacturer().getId());
            updateStatement.setLong(3, car.getId());
            updateStatement.executeUpdate();
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't update " + car, throwable);
        }
        deleteConnectionBetweenCarAndDriver(car);
        createConnectionBetweenCarAndDriver(car);
        return car;
    }

    private Car deleteConnectionBetweenCarAndDriver(Car car) {
        String deleteQuery = "DELETE FROM cars_drivers WHERE car_id=?;";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            deleteStatement.setLong(1, car.getId());
            deleteStatement.executeUpdate();
            return car;
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't delete cars drivers for " + car, throwable);
        }
    }

    @Override
    public boolean delete(Long id) {
        String deleteQuery = "UPDATE cars SET deleted=true WHERE id=?;";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            deleteStatement.setLong(1, id);
            return deleteStatement.executeUpdate() > 0;
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't delete car by id " + id, throwable);
        }
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        String getCarQuery = "SELECT car_id AS id, model, manufacturer_id, name, country "
                + "FROM manufacturers JOIN (SELECT * FROM cars JOIN cars_drivers "
                + "ON cars.id = cars_drivers.car_id "
                + "WHERE cars.deleted=false AND cars_drivers.driver_id=?) "
                + "AS full_car_table "
                + "ON manufacturers.id = full_car_table.manufacturer_id;";
        List<Car> driverCars = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getCarsStatement = connection.prepareStatement(getCarQuery)) {
            getCarsStatement.setLong(1, driverId);
            ResultSet resultSet = getCarsStatement.executeQuery();
            while (resultSet.next()) {
                driverCars.add(createCarWithoutDrivers(resultSet));
            }
        } catch (SQLException throwable) {
            throw new DataProcessingException("Couldn't get list of car id", throwable);
        }
        for (Car car : driverCars) {
            getDriversForCar(car.getId());
        }
        return driverCars;
    }
}
