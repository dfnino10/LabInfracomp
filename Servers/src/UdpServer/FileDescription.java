package UdpServer;

import java.io.Serializable;



public class FileDescription implements Serializable {

	public FileDescription() {
	}

	private static final long serialVersionUID = 1L;

	private String sourceDirectory;
	private String filename;
	private long fileSize;
	private String status;
	private String hash;
	private int numPaquetes;
	

	public int getNumPaquetes() {
		return numPaquetes;
	}

	public void setNumPaquetes(int numPaquetes) {
		this.numPaquetes = numPaquetes;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String toString(){
		return "Name: " + filename + " Size: "+ fileSize+
				" #Chunks: " + numPaquetes + " Hash: " + hash;
	}
	

}