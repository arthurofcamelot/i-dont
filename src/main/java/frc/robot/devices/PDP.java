/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.devices;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import frc.robot.logging.Logger;
import frc.robot.logging.LoggerRelations;

/**
 * Add your docs here.
 */
public class PDP implements Logger{

    PowerDistributionPanel PDP_panel;

    private static double[] oldVoltages = new double[4];
    private int index = 0;

    public PDP(){
        PDP_panel = new PowerDistributionPanel();
    }

    @Override
    public double[] getValues(double[] values) {
        values[LoggerRelations.PDP_VOLTAGE.value] = getPDPVoltage();
        values[LoggerRelations.PDP_CURRENT.value] = getTotalPDPCurrent();
        return values;
    }

    public double getPDPVoltage(){
        return PDP_panel.getVoltage();
    }

    /**
     * @return the minumum of the last 4 voltages read from the pdp
     */
    public double getMinimumPDPVoltage(){
        oldVoltages[index]=PDP_panel.getVoltage();
        index++;
        if(index == oldVoltages.length){
            index=0;
        }
        double out = 1000;
        for(int i = 0; i<oldVoltages.length; i++){
            if (oldVoltages[i] < out){
                out = oldVoltages[i];
            }
        }
        return out;
    }

    public double getTotalPDPCurrent(){
        return PDP_panel.getTotalCurrent();
    }

    public double getChanelPDPCurrent(int PDP_Channel){
        return PDP_panel.getCurrent(PDP_Channel);
    }

    public double getChanelPDPCurrentPrecent(int PDP_Channel){
        return getChanelPDPCurrent(PDP_Channel)/getTotalPDPCurrent();
    }


}