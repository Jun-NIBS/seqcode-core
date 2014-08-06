/*
 * Created on Apr 27, 2006
 */
package edu.psu.compbio.seqcode.gse.gsebricks.verbs.sequence;

import java.io.*;
import java.util.*;

import edu.psu.compbio.seqcode.gse.gsebricks.iterators.SingleIterator;
import edu.psu.compbio.seqcode.gse.gsebricks.verbs.*;
import edu.psu.compbio.seqcode.gse.utils.*;

/**
 * @author tdanford
 */
public class FASTALoader implements Expander<File,Pair<String,String>> {
    
    public FASTALoader() {
    }

    /* (non-Javadoc)
     * @see edu.psu.compbio.seqcode.gse.gsebricks.verbs.Expander#execute(java.lang.Object)
     */
    public Iterator<Pair<String, String>> execute(File a) {
        SingleIterator<File> fitr = new SingleIterator<File>(a);
        Iterator<String> lines = new ExpanderIterator<File,String>(new FileLineExpander(), fitr);
        return new LazyFASTAIterator(lines);
    }
    
    private static class LazyFASTAIterator implements Iterator<Pair<String,String>> { 
        
        private Iterator<String> litr;
        private String nextHeader;
        
        public LazyFASTAIterator(Iterator<String> _litr) { 
            litr = _litr;
            nextHeader = null;
            if(litr.hasNext()) { nextHeader = litr.next(); }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return nextHeader != null; 
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Pair<String, String> next() {
            StringBuilder sb = new StringBuilder();
            String header = nextHeader;
            nextHeader = null;
            
            String line = null;
            while(litr.hasNext() && !(line = litr.next()).startsWith(">")) { 
                sb.append(line.trim());
            }
            
            if(litr.hasNext()) { 
                nextHeader = line.trim(); 
            }

            return new Pair<String,String>(header, sb.toString());
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
