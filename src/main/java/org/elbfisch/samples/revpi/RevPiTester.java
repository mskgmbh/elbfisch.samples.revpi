/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : RevPiTester.java
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

import org.jpac.Module;

/**
 *
 * @author berndschuster
 */
public class RevPiTester{
    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("restriction")	
    public static void main(String[] args) {
        Module module = new MainModule();
        module.start();
        try{
            javafx.application.Application.launch(DashboardApplication.class, args);
        } catch(UnsupportedOperationException exc){
            System.out.println(">>> Info: failed to open display. Start application from graphical desktop to show Elbfisch dashboards");
            System.out.println(">>> Info: Elbfisch application started headless ...");
        } catch(NoClassDefFoundError exc){
            System.out.println(">>> Info: failed to initialize JavaFX. Install Gluon JavaFXPorts or OpenJFX");
            System.out.println(">>> Info: Elbfisch application started headless ...");
        }
    }
}
