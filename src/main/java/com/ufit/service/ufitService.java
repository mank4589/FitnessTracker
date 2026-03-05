package com.ufit.service;

import com.ufit.model.ufit;
import com.ufit.logic.HealthCalculator;
import com.ufit.repository.ufitRepository;
import org.springframework.stereotype.Service;

@Service
public class ufitService {

    public final ufitRepository repository;

    public ufitService(ufitRepository repository) {
        this.repository = repository;
    }

    public ufit saveFullHealthReport(ufit report) {
        // 1. Basic Calculations
        report.setBmi(HealthCalculator.calculateBMI(report.getWeight(), report.getHeight()));
        
        double calculatedBmr = HealthCalculator.calculateBMR(
            report.getWeight(), report.getHeight(), report.getAge(), report.getGender()
        );
        report.setBmr(calculatedBmr);

        // 2. New Logic: Body Fat and TDEE
        report.setBodyFat(HealthCalculator.calculateBodyFat(
            report.getWaist(), report.getNeck(), report.getHeight() * 100, report.getGender()
        ));

        report.setTdee(HealthCalculator.calculateTDEE(
            calculatedBmr, report.getActivityLevel()
        ));

        report.setIdealWeight(HealthCalculator.calculateIdealWeight(
            report.getHeight() * 100, report.getGender()
        ));

        // 3. Save the fully calculated report to the database
        return repository.save(report);
    }
}