package org.seqcode.data.pegr;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a sample in the PughLab DB
 * @author mahony
 *
 *
	CREATE TABLE `PughLabSampleInfo` (
	  `SAMPLE_ID` int(11) unsigned NOT NULL AUTO_INCREMENT,
	  `PROJECT_ID` char(40) DEFAULT NULL,
	  `RUN_ID` char(10) DEFAULT NULL,
	  `NAME` char(255) DEFAULT NULL,
	  `TARGET` char(40) DEFAULT NULL,
	  `ANTIBODY` char(40) DEFAULT NULL,
	  `ANTIBODY_VOLUME` char(10) DEFAULT NULL,
	  `STRAIN` char(40) DEFAULT NULL,
	  `MUTATION` char(40) DEFAULT NULL,
	  `MEDIA` char(40) DEFAULT NULL,
	  `PERTURB` char(40) DEFAULT NULL,
	  `ASSAY_CODE` char(10) DEFAULT NULL,
	  `REP_CODE` char(10) DEFAULT NULL,
	  `USERID` char(10) DEFAULT NULL,
	  `SEQ_DATE` char(10) DEFAULT NULL,
	  `UNIQ_ID` char(40) NOT NULL DEFAULT '',
	  `PRI_GENOME` char(20) DEFAULT NULL,
	  `SEQ_BY` char(20) DEFAULT NULL,
	  `COMMENT` blob,
	  `REF_GENOME` char(80) DEFAULT NULL,
	  `SUCCESS` enum('Good','Failed','Pending') DEFAULT 'Pending',
	  `INDEX_COUNT` int(11) unsigned NOT NULL DEFAULT '0',
	  `PUBLIC_FLAG` enum('Y','N') DEFAULT 'N',
	  `SRA_ID` char(15) DEFAULT NULL,
	  PRIMARY KEY (`SAMPLE_ID`,`UNIQ_ID`)
	) ENGINE=MyISAM AUTO_INCREMENT=7106 DEFAULT CHARSET=utf8
 *
 *
 */
public class PughLabSampleInfo implements Comparable<PughLabSampleInfo>{

	private int sample_id;
	private String project_id;
	private String run_id;
	private String name;
	private String target;
	private String antibody;
	private String antibody_volume;
	private String strain;
	private String mutation;
	private String media;
	private String perturb;
	private String assay_code;
	private String rep_code;
	private String userid;
	private String seq_date;
	private String uniq_id;
	private String pri_genome;
	private String seq_by;
	private String comment;
	private String ref_genome;
	private String success;
	private int index_count;
	private String public_flag;
	private String sra_id;
	
	public PughLabSampleInfo(ResultSet rs)  throws SQLException {
		sample_id = rs.getInt(1);
		project_id = rs.getString(2);
		run_id = rs.getString(3);
		name = rs.getString(4);
		target = rs.getString(5);
		antibody = rs.getString(6);
		antibody_volume  = rs.getString(7);
		strain = rs.getString(8);
		mutation = rs.getString(9);
		media = rs.getString(10);
		perturb = rs.getString(11);
		assay_code = rs.getString(12);
		rep_code = rs.getString(13);
		userid = rs.getString(14);
		seq_date = rs.getString(15);
		uniq_id = rs.getString(16);
		pri_genome = rs.getString(17);
		seq_by = rs.getString(18);
		comment  = rs.getString(19);
		ref_genome = rs.getString(20);
		success = rs.getString(21);
		index_count = rs.getInt(22);
		public_flag = rs.getString(23);
		sra_id = rs.getString(24);
	}
	
	public int getSampleID() {return sample_id;}
	public String getProjectID() {return  project_id;}
	public String getRunID() {return  run_id;}
	public String getName() {return  name;}
	public String getTarget() {return  target;}
	public String getAntibody() {return  antibody;}
	public String getAntibodyVol() {return  antibody_volume;}
	public String getStrain() {return  strain;}
	public String getMutation() {return  mutation;}
	public String getMedia() { return media;}
	public String getPerturb() {return  perturb;}
	public String getAssayCode() {return  assay_code;}
	public String getRepCode() {return  rep_code;}
	public String getUserID() {return  userid;}
	public String getSeqDate() {return  seq_date;}
	public String getUniqID() {return  uniq_id;}
	public String getGenome() {return  pri_genome;}
	public String getSeqBy() {return  seq_by;}
	public String getComment() {return  comment;}
	public String getRefGenome() {return  ref_genome;}
	public String getSuccess() {return  success;}
	public int getIndexCount() {return  index_count;}
	public String getPublicFlag() {return  public_flag;}
	public String getSRAID() {return  sra_id;}
	
	/**
	 * Remake name here - as I'm not sure if database entries always reflect updates
	 */
	public String buildName(){
		String ab = antibody_volume==null ? antibody : antibody+"-"+antibody_volume;
		String repID = assay_code+rep_code;
		return target+"_"+ab+"_"+strain+"_"+mutation+"_"+media+"_"+perturb+"_"+repID+"_"+userid+"-"+seq_date+"_"+uniq_id;
	}
	
	/**
	 * Returns Pugh Lab format experiment name
	 */
	public String toString() { 
    	return buildName();
    }

	//Below are some SeqData naming translations
	public String getSeqDataTargetName() {
		String ab = antibody_volume==null ? antibody : antibody+"-"+antibody_volume;
		String tname = target+"_"+ab;
		while(tname.endsWith("_-")){ tname = tname.substring(0,tname.lastIndexOf("_-"));}
		while(tname.startsWith("-_")){ tname = tname.replaceFirst("-_", "");}
		return tname;
	}
	public String getSeqDataConditionName() {
		String cname = strain+"_"+mutation+"_"+media+"_"+perturb;
		while(cname.endsWith("_-")){ cname = cname.substring(0,cname.lastIndexOf("_-"));}
		while(cname.startsWith("-_")){ cname = cname.replaceFirst("-_", "");}
		return cname;
	}
	public String getSeqDataCelllineName() {
		String cname= strain+"_"+mutation;
		while(cname.endsWith("_-")){ cname = cname.substring(0,cname.lastIndexOf("_-"));}
		while(cname.startsWith("-_")){ cname = cname.replaceFirst("-_", "");}
		return cname;
	}
	public String getSeqDataReplicateName() {
		String repID = assay_code+rep_code;
		String rname = repID+"_"+userid+"-"+seq_date+"_"+uniq_id;
		while(rname.endsWith("_-")){ rname = rname.substring(0,rname.lastIndexOf("_-"));}
		while(rname.startsWith("-_")){ rname = rname.replaceFirst("-_", "");}
		return rname;
	}
	public String convertAssayCode(){
		String assay = "UNKNOWN";
	    if(assay_code.equals("XO")){ assay = "CHIPEXO";
	    }else if(assay_code.equals("CS")){ assay = "CHIPSEQ";
	    }else if(assay_code.equals("IN") || assay_code.equals("NA")){ assay = "INPUT";
	    }else if(assay_code.equals("KS")){ assay = "KWIKSEQ";
	    }else if(assay_code.equals("BS")){ assay = "BISSEQ";
	    }else if(assay_code.equals("CT")){ assay = "CHIPTOPO";
	    }else if(assay_code.equals("PS")){ assay = "PERMSEQ"; 
	    }else if(assay_code.equals("MN")){ assay = "MNASECHIPSEQ";
	    }
	    if(target.toLowerCase().matches("no-tag") || target.toLowerCase().matches("no-ab") || target.toLowerCase().matches("None") || target.toLowerCase().matches("IgG")){ 
	    	assay = "CONTROL";
	    }
	    return assay;
	}
	
	
    
    public int compareTo(PughLabSampleInfo o){
    	//String name = buildName();
    	//String oname = o.buildName();
    	return uniq_id.compareTo(o.uniq_id);
    }
    public boolean equals(Object o) { 
        if(!(o instanceof PughLabSampleInfo)) { return false; }
        PughLabSampleInfo c = (PughLabSampleInfo)o;
        if(sample_id != c.sample_id) { return false; }
        return true;
    }
    
    public int hashCode() { 
        int code = 17;
        code += sample_id; code *= 37;
        return code;
    }
    
    public static PreparedStatement createLoadAll(java.sql.Connection c) throws SQLException { 
        return c.prepareStatement("select sample_id, project_id, run_id, name, target, antibody, antibody_volume, strain, mutation, media, perturb, assay_code, rep_code, userid, seq_date, uniq_id, pri_genome, seq_by, comment, ref_genome, success, index_count, public_flag, sra_id " +
        		"from PughLabSampleInfo");
    }
    
    public static PreparedStatement createLoadBySampleID(java.sql.Connection c) throws SQLException { 
        return c.prepareStatement("select sample_id, project_id, run_id, name, target, antibody, antibody_volume, strain, mutation, media, perturb, assay_code, rep_code, userid, seq_date, uniq_id, pri_genome, seq_by, comment, ref_genome, success, index_count, public_flag, sra_id " +
        		"from PughLabSampleInfo where sample_id=?");
    }

    public static PreparedStatement createLoadByUniqID(java.sql.Connection c) throws SQLException { 
        return c.prepareStatement("select sample_id, project_id, run_id, name, target, antibody, antibody_volume, strain, mutation, media, perturb, assay_code, rep_code, userid, seq_date, uniq_id, pri_genome, seq_by, comment, ref_genome, success, index_count, public_flag, sra_id " +
        		"from PughLabSampleInfo where uniqid=?");
    }

}
