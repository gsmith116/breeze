package scalanlp.data;

/*
 Copyright 2009 David Hall, Daniel Ramage
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at 
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
*/


import scala.collection.Map;
import scalax.io.Implicits._;
import java.io.File;
import counters.Counters._;
import process._;


/**
* Represents a Bag of (Words). Interfaces should prefer to use Example[L,Map[W,Int]].
* This is intended as a convenience class.
*
* @author dlwh
*/
class Bag[+L,W](val id:String, val label:L, words: Map[W,Int]) extends Example[L,Map[W,Int]] {
  def features = words;
}

object Bag {
  /**
  * Reads in a file as a bag of lower case words. Only alphanumeric characters
  * are read. Strings are lowercased and locale stopwords are removed.
  * The label is set as the name of the directory containing the file.
  *
  * @author dlwh
  */
  def fromFile(file: File) = {
    val words = 
      for( line <- file.lines;
           word <- line.split("[^A-Za-z0-9]")
         if word != "" && RemoveStopwords(word) )
         yield word.toLowerCase;
    val cter = words.elements.acquireFor(count(_));
    new Bag[String,String](file.getName,file.getParentFile.getName,cter);
  }

  /** Converts a Document to a Bag by taking each (field,words)
  * pair and converting it to a sequence of "field&lt;sep&gt;word" strings.
  * The result is then aggregated into a counter.
  */
  def fromDocument[L](doc: LabeledDocument[L,String], sep:String): Bag[L,String] = {
    val words = 
      for((field,words) <- doc.fields;
          w <- words)
          yield field + sep + w;
    new Bag(doc.id,doc.label, count(words));
    
  }

  def fromDocument[L](doc:LabeledDocument[L,String]): Bag[L,String] = fromDocument(doc,"/");

  def fromLabeledText[L](text:LabeledText[L]): Bag[L,String] = {
    val ctr = count(AlphaTokenize(text.contents) filter RemoveStopwords map(_.toLowerCase));
    new Bag[L,String](text.id,text.label,ctr);
  }
}