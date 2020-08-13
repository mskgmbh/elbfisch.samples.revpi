/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : SineWaveModule.java
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
import org.jpac.Module;
import org.jpac.NextCycle;
import org.jpac.NthCycle;
import org.jpac.OutputInterlockException;
import org.jpac.ProcessException;
import org.jpac.ShutdownRequestException;
import org.jpac.SignedInteger;
import org.jpac.IoDirection;
import org.jpac.vioss.IoSignedInteger;


/**
 *
 * @author stefannouza
 */
public class SineWaveModule extends Module {

    protected SignedInteger analogOut;
    
    public SineWaveModule(Module containingModule, boolean runningOnRevPi) {
        super(containingModule, "SineWaveModule");
        try{
            if (runningOnRevPi){
                try {    
                    analogOut  = new IoSignedInteger(this, "AnalogOut", new URI("revpi://localhost/OutputValue_1"), IoDirection.OUTPUT);            
                } catch (InconsistencyException exc) {
                    Log.info("AIO module presumably not installed. Instantiate regular signal for 'AnalogOut'");
                    analogOut  = new SignedInteger(this, "AnalogOut");            
                } catch(Exception exc){
                    Log.error("Error:", exc);            
                }
            } 
            else {
                //not running on RevPi
                analogOut  = new SignedInteger(this, "AnalogOut");            
            }
        } catch(Exception exc){
            Log.error("Error: ", exc);
        }
    }
    
    @Override
    protected void work() throws ProcessException {
        double  rad       = 0;   
        boolean done      = false;
        try{
            Log.info("started");
            do{
                try{
                    rad += 2 * Math.PI/50;
                    analogOut.set((int)(Math.round((Math.sin(rad) * Short.MAX_VALUE/4))));
                    new NthCycle(2).await();//put out a new sine wave value every 2nd cycle                    
                }
                catch(ShutdownRequestException exc){
                    done = true;
                }
                catch(Exception exc){
                    Log.error("Error", exc);
                    done = true;
                }
            }
            while (!done);
        }
        finally{
            analogOut.set(0);
            new NextCycle().await();
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

    /**
     * @return the analogOut
     */
    public SignedInteger getAnalogOut() {
        return analogOut;
    }
}
