package taxi.service.impl;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.dao.CarDao;
import taxi.lib.Inject;
import taxi.lib.Service;
import taxi.model.Car;
import taxi.model.Driver;
import taxi.service.CarService;

@Service
public class CarServiceImpl implements CarService {
    private static final Logger logger = LogManager.getLogger(CarServiceImpl.class);
    @Inject
    private CarDao carDao;

    @Override
    public void addDriverToCar(Driver driver, Car car) {
        logger.debug("Method addDriverToCar was called. " + driver + ". - " + car);
        car.getDrivers().add(driver);
        carDao.update(car);
    }

    @Override
    public void removeDriverFromCar(Driver driver, Car car) {
        logger.debug("Method removeDriverFromCar was called. " + driver + ". - " + car);
        car.getDrivers().remove(driver);
        carDao.update(car);
    }

    @Override
    public List<Car> getAllByDriver(Long driverId) {
        logger.debug("Method getAllByDriver was called. Driver id - " + driverId);
        return carDao.getAllByDriver(driverId);
    }

    @Override
    public Car create(Car car) {
        logger.debug("Method create was called. " + car);
        return carDao.create(car);
    }

    @Override
    public Car get(Long id) {
        logger.debug("Method get was called. Car id - " + id);
        return carDao.get(id).get();
    }

    @Override
    public List<Car> getAll() {
        logger.debug("Method getAll was called.");
        return carDao.getAll();
    }

    @Override
    public Car update(Car car) {
        logger.debug("Method update was called. " + car);
        return carDao.update(car);
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("Method delete was called. Car id - " + id);
        return carDao.delete(id);
    }
}
