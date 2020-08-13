/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : TriggerModule.java
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
import org.jpac.Logical;
import org.jpac.Module;
import org.jpac.NextCycle;
import org.jpac.OutputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessException;
import org.jpac.ShutdownRequestException;
import org.jpac.SignalInvalidException;
import org.jpac.SignedInteger;
import org.jpac.IoDirection;
import org.jpac.vioss.IoLogical;
import org.jpac.vioss.IoSignedInteger;


public class TriggerModule extends Module {
    final int ANALOGTHRESHOLD = Short.MAX_VALUE/5;

    protected SignedInteger analogIn;
    protected Logical       trigger;
    protected boolean       aIoModuleInstalled;
    protected boolean       dIoModuleInstalled;
    
    LedControlModule  ledTestModule;
    SineWaveModule sineWaveModule;
        
    public TriggerModule(Module containingModule, boolean runningOnRevPi) {
        super(containingModule, "TriggerModule");
        try {
            if (runningOnRevPi){
                try {
                    analogIn = new IoSignedInteger(this, "AnalogIn", new URI("revpi://localhost/InputValue_1"), IoDirection.INPUT);
                    aIoModuleInstalled = true;
                } catch (InconsistencyException exc) {
                    Log.info("AIO module presumably not installed. Instantiate regular signal for 'AnalogIn'");
                    analogIn = new SignedInteger(this, "AnalogIn");            
                    aIoModuleInstalled = false;
                }
                try {
                    trigger = new IoLogical(this, "Trigger", new URI("revpi://localhost/O_1"), IoDirection.OUTPUT);
                    dIoModuleInstalled = true;
                } catch (InconsistencyException exc) {
                    Log.info("DIO module presumably not installed. Instantiate regular signal for 'Trigger'");
                    trigger = new Logical(this, "Trigger");
                    dIoModuleInstalled = false;
                }
            } 
            else {
                analogIn = new SignedInteger(this, "AnalogIn");            
                trigger  = new Logical(this, "Trigger");
            }
        } catch (Exception exc) {
            Log.error("Error:", exc);
        }
    }
        
    @Override
    protected void work() throws ProcessException {
        boolean inputValid = false;
        boolean done       = false;
        try{
            Log.info("started");
            do{
                analogIn.becomesValid().await();
                inputValid = true;
                trigger.set(false);
                do{
                    try{
                        if (analogIn.get() > 0){
                            Log.info("analogIn pos. {}", analogIn.get());
                            analogIn.exceeds(ANALOGTHRESHOLD).await();
                            Log.info("analogIn above pos. threshold {}", analogIn.get());
                            trigger.set(true);
                            analogIn.fallsBelow(ANALOGTHRESHOLD).await();
                            Log.info("analogIn below pos. threshold {}", analogIn.get());
                            trigger.set(false);
                            analogIn.fallsBelow(0).await();
                        } else {
                            //Log.info("analogIn neg.");
                            analogIn.fallsBelow(-ANALOGTHRESHOLD).await();
                            Log.info("analogIn below neg. threshold {}", analogIn.get());
                            trigger.set(true);
                            analogIn.exceeds(-ANALOGTHRESHOLD).await();
                            Log.info("analogIn above neg. threshold {}", analogIn.get());
                            trigger.set(false);                            
                            analogIn.exceeds(0).await();
                        }
                    }
                    catch(ShutdownRequestException exc){
                        done = true;
                    }
                    catch(SignalInvalidException exc){
                        Log.error("Error", exc);
                        inputValid = false;
                    }
                }
                while(!done && inputValid);
            }
            while(!done);
        }
        finally{
            trigger.set(false);
            new NextCycle().await();
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

    public boolean isAioModuleInstalled() {
        return aIoModuleInstalled;
    }

    public boolean isDioModuleInstalled() {
        return dIoModuleInstalled;
    }

    public SignedInteger getAnalogIn() {
        return analogIn;
    }
}
