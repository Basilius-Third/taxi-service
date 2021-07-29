package taxi.service;

import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import taxi.dao.DriverDao;
import taxi.lib.Inject;
import taxi.lib.Service;
import taxi.model.Driver;

@Service
public class DriverServiceImpl implements DriverService {
    private static final Logger logger = LogManager.getLogger(DriverServiceImpl.class);
    @Inject
    private DriverDao driverDao;

    @Override
    public Driver create(Driver driver) {
        logger.debug("Method create was called. " + driver);
        return driverDao.create(driver);
    }

    @Override
    public Driver get(Long id) {
        logger.debug("Method get was called. Driver id - " + id);
        return driverDao.get(id).get();
    }

    @Override
    public List<Driver> getAll() {
        logger.debug("Method getAll was called.");
        return driverDao.getAll();
    }

    @Override
    public Driver update(Driver driver) {
        logger.debug("Method update was called. " + driver);
        return driverDao.update(driver);
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("Method delete was called. Driver id - " + id);
        return driverDao.delete(id);
    }

    @Override
    public Optional<Driver> findByLogin(String login) {
        logger.debug("Method findByLogin was called. Driver login - " + login);
        return driverDao.findByLogin(login);
    }
}
