/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : LedControlModule.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   :
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller. jPac is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the jPac If not, see <http://www.gnu.org/licenses/>.
 */
package org.elbfisch.samples.revpi;

import java.net.URI;
import org.jpac.InconsistencyException;
import org.jpac.InputInterlockException;
import org.jpac.IoDirection;
import org.jpac.Logical;
import org.jpac.Module;
import org.jpac.OutputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessException;
import org.jpac.ShutdownRequestException;
import org.jpac.SignalInvalidException;
import org.jpac.SignedInteger;
import org.jpac.vioss.IoLogical;
import org.jpac.vioss.IoSignedInteger;
import org.jpac.vioss.revpi.RevPi;


public class LedControlModule extends Module {

    SignedInteger revPiLed;
    Logical       trigger;
        
    public LedControlModule(Module containingModule, boolean runningOnRevPi) {
        super(containingModule, "LedControlModule");
        try {
            if (runningOnRevPi){
                try {
                    trigger = new IoLogical(this, "Trigger", new URI("revpi://localhost/I_1"), IoDirection.INPUT);
                } catch (InconsistencyException exc) {
                    Log.info("DIO module presumably not installed. Instantiate regular signal for 'Trigger'");
                    trigger = new Logical(this, "Trigger");
                }            
                revPiLed    = new IoSignedInteger(this, "RevPiLED", new URI("revpi://localhost/RevPiLED"), IoDirection.OUTPUT);
            } 
            else{
                trigger  = new Logical(this, "Trigger"); 
                revPiLed = new SignedInteger(this, "RevPiLED");                
            }
        } catch (Exception exc) {
            Log.error("Error:", exc);
        }
    }

    @Override
    protected void work() throws ProcessException {
        int     led1Ctl      = 0;
        int     led2Ctl      = 0;
        boolean triggerValid = false;
        boolean done         = false;
        PeriodOfTime delay   = new PeriodOfTime(1 * sec);
        try{
            Log.info("started");
            do{
                //TODO trigger.becomesValid().await();
                triggerValid = true;
                do{
                    try{
                    	Log.info("toggling leds ...");
                    	delay.await();//trigger.becomes(true).await();//wait for positive edge of trigger
                        led1Ctl = RevPi.CoreV12.LedA1Green.value;
                        led2Ctl = RevPi.CoreV12.LedA2Orange.value;
                        revPiLed.set(led1Ctl + led2Ctl);
                        delay.await();//trigger.becomes(true).await();//wait for positive edge of trigger
                        led1Ctl = RevPi.CoreV12.LedA1Orange.value;
                        led2Ctl = RevPi.CoreV12.LedA2Red.value;
                        revPiLed.set(led1Ctl + led2Ctl);
                        delay.await();//trigger.becomes(true).await();//wait for positive edge of trigger
                        led1Ctl = RevPi.CoreV12.LedA1Red.value;
                        led2Ctl = RevPi.CoreV12.LedA2Off.value;
                        revPiLed.set(led1Ctl + led2Ctl);
                        delay.await();//trigger.becomes(true).await();//wait for positive edge of trigger
                    }
                    catch(ShutdownRequestException exc){
                        done = true;
                    }
                    catch(SignalInvalidException exc){
                        Log.error("Error", exc);
                        triggerValid = false;
                    }
                }
                while(!done && triggerValid);
            }
            while(!done);
        }
        finally{
            Log.info("finished");
        }
    }
    
    public Logical getTrigger(){
        return trigger;
    }

    @Override
    protected void preCheckInterlocks() throws InputInterlockException {
    }

    @Override
    protected void postCheckInterlocks() throws OutputInterlockException {
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
