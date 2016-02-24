/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.service.PasswordValidationService;
import java.util.Arrays;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;

/**
 *
 * @author Michel
 */
public class PasswordValidationServiceImpl implements PasswordValidationService {

    private PasswordValidator validator;

    public PasswordValidationServiceImpl() {
        LengthRule r1 = new LengthRule(8);

        validator = new PasswordValidator(Arrays.asList((Rule) r1));
    }

    @Override
    public boolean validatePassword(String password) {
        PasswordData data = PasswordData.newInstance(password, null, null);
        RuleResult result = validator.validate(data);
        return result.isValid();
    }

}
