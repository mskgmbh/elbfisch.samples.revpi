/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : MainModule.java
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

import java.io.File;
import org.jpac.ImpossibleEvent;
import org.jpac.InputInterlockException;
import org.jpac.Module;
import org.jpac.OutputInterlockException;
import org.jpac.ProcessException;


/**
 *
 * @author stefannouza
 */
public class MainModule extends Module {
    
    LedControlModule ledControlModule;
    SineWaveModule   sineWaveModule;
    TriggerModule    triggerModule;
    boolean          runningOnRevPi;
        
    public MainModule() {
        super(null, "Main");
        try{
            runningOnRevPi   = new File("/dev/piControl0").exists();
            ledControlModule = new LedControlModule(this, runningOnRevPi);
            sineWaveModule   = new SineWaveModule(this, runningOnRevPi);
            triggerModule    = new TriggerModule(this, runningOnRevPi);
        } catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    @Override
    public void start(){
      //connect trigger output of triggerModule to trigger input of ledControlModule
      if (!runningOnRevPi || !triggerModule.isDioModuleInstalled()){
        //if not running on RevPi or DIO module not installed, connect trigger output of the trigger module directly to the led control module (shortcut)
        triggerModule.getTrigger().connect(ledControlModule.getTrigger());
      }
      if (!runningOnRevPi || !triggerModule.isAioModuleInstalled()){
        //if not running on RevPi or AIO module not installed, connect sine wave signal directly to the trigger module (shortcut)
        sineWaveModule.getAnalogOut().connect(triggerModule.getAnalogIn());
      }
      //start contained modules
      ledControlModule.start();
      sineWaveModule.start();
      triggerModule.start();
      //then start myself
      super.start();  
    }
    
    @Override
    protected void work() throws ProcessException {
        try{
          //nothing useful to do here. Just wait, until the application is closed
          new ImpossibleEvent().await();
        } finally{
            Log.info("finished");
        }
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
