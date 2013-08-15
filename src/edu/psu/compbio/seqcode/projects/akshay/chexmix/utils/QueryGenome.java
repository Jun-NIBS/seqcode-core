package edu.psu.compbio.seqcode.projects.akshay.chexmix.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import edu.psu.compbio.seqcode.projects.akshay.chexmix.datasets.*;

public abstract class QueryGenome {
	public String genomepath;
	public String chr;
	public int midpoint;
	public int range;
	public abstract void fillGenomePath();
	public abstract void fillGenomePath(String path);
	public QueryGenome(String chr, int midpoint, int range) {
		this.chr = chr;
		this.midpoint = midpoint;
		this.range = range;
	}
	public Seq getSeq(String orientation) throws IOException{
		String currdir = System.getProperty("user.dir");
		File file;
		file = new File(currdir+"/temp/"+"tempSeqQuery.bed");
		if(file.exists()){
			file.delete();
		}
		FileWriter fstream = new FileWriter(currdir+"/temp/tempSeqQuery.bed", false);
		BufferedWriter out = new BufferedWriter(fstream);
		String content = this.chr+"\t"+Integer.toString(this.midpoint-this.range)+"\t"+Integer.toString(this.midpoint+this.range)+"\t"+"*"+"\t"+"*"+"\t"+orientation+"\n";
		out.write(content);
		out.close();
		List<String> command = new ArrayList<String>();
		command.add("bedtools");
		command.add("getfasta");
		command.add("-s");
		command.add("-fi");
		command.add(this.genomepath+"/"+this.chr+".fa");
		command.add("-bed");
		command.add(currdir+"/temp/tempSeqQuery.bed");
		command.add("-fo");
		command.add("temp/temp.fa");
		ProcessBuilder pb = new ProcessBuilder(command);
		System.out.println(pb.command());
		try{
			Process shell = pb.start();
			shell.waitFor();
		}
		catch (IOException e) {
			System.out.println("Error occured while executing Linux command. Error Description: "
			+ e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("Error occured while executing Linux command. Error Description: "
					+ e.getMessage());
		}
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(currdir+"/temp/"+"temp.fa"));
		String currentline = br.readLine();
		String tempseq = null;
		while(!currentline.startsWith(">") && currentline != null){
			tempseq = currentline;
			System.out.println(currentline);
			currentline = br.readLine();
		}
		br.close();
		Seq ret = new Seq(this.midpoint, this.range, this.chr, orientation, tempseq);
		return ret;
	}
}
