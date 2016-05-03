
public class Call {
	private int id;
	private int duration;
	private int time;
	private int x;
	private int y;
	
	// We assign the data in the constructor. After that, this object
	// will be read-only.
	public Call(int id, int x, int y, int time, int duration) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.time = time;
		this.duration = duration;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getTime() {
		return time;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getId() {
		return id;
	}
	
	// hash for the hashMap
	@Override
	public int hashCode() {
		return (this.x+100) + (this.y+100)*201 + this.time*201*201;
	}
	
	// The id field uniquely identifies every call
	@Override
	public boolean equals(Object obj) {
		Call other = (Call) obj;
		return this.id == other.getId();
	}
}
