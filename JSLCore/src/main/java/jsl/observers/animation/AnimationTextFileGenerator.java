/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jsl.observers.animation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AnimationTextFileGenerator implements AnimationMessageHandlerIfc {

    private File myFile;
    protected PrintWriter myPrintWriter;
    protected StringBuilder myAnimationMessage;

	public AnimationTextFileGenerator(String name) throws IOException {
		this(null, name);
	}

	public AnimationTextFileGenerator(String directory, String name) throws IOException {
        if (directory == null)
        		directory = "jslOutput";
        
        File d = new File(directory);
        d.mkdir();
        
        myFile = new File(d, makeFileName(name));
        
        myPrintWriter = new PrintWriter(new FileWriter(myFile),true);

		myAnimationMessage = new StringBuilder();

	}

	public boolean isStarted() {
		return (myAnimationMessage.length() > 0);
	}
	
	public void beginMessage() {
		if (myAnimationMessage.length() > 0)
			myAnimationMessage.delete(0, myAnimationMessage.length()+1);
	}

	public void append(double value) {
		myAnimationMessage.append(value);
	}

	public void append(long value) {
		myAnimationMessage.append(value);		
	}

	public void append(String value) {
		myAnimationMessage.append(value);		
	}
	
	public void commitMessage() {
		if (myAnimationMessage.length() > 0)
			myPrintWriter.println(myAnimationMessage);	
	}

    private String makeFileName(String name){
        //construct filename to ensure .txt
        String s;
        int dot = name.lastIndexOf(".");
        
        if ( dot == -1 ) // no period found
            s = name + ".txt";
        else // period found
            s = name.substring(dot) + "txt";
        
        return(s);
    }

}
