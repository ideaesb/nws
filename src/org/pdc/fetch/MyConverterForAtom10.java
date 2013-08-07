package org.pdc.fetch;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.impl.ConverterForAtom10;


/**
 * Getting XML String value of SyndEntry with ROME.  Required to access protected methods of base class 
 * @author Neozaru user of Stackoverflow.  http://stackoverflow.com/a/16238401/2655908
 *
 */
public class MyConverterForAtom10 extends ConverterForAtom10 {

public SyndEntry syndEntryFromEntry( Entry entry ) {
    return this.createSyndEntry(null,entry,false);
}

public Entry entryFromSyndEntry( SyndEntry syndentry ) {
    return this.createAtomEntry(syndentry);
}

}
