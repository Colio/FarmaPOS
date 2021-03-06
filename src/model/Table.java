package model;

import java.util.ArrayList;
import java.util.List;

import model.log.ActionLog;
import model.log.AddItemToTableActionLog;
import model.log.RemoveItemFromTableActionLog;
import exceptions.*;

public class Table {

	private String name;
	private Room room;
	private ArrayList<ItemOnTable> content;
	private AbstractTableState state;
	private	Measures location;
	private List<ActionLog> actionsLog;


	public Measures getLocation() {							return location;					}
	public AbstractTableState getState() {					return state;						}
	public List<ItemOnTable> getContent() {					return content;						}
	public String getName() {										return name;						}
	public List<ActionLog> getActionsLog() {						return actionsLog;					}
	public Room getRoom() {											return room;						}
	
	public void setLocation(Measures location) {					this.location = location;			}
	public void setName(String name) {								this.name = name;					}
	public void setState(AbstractTableState state) {				this.state = state;					}
	public void setActionsLog(List<ActionLog> actionsLog) {			this.actionsLog = actionsLog;		}
	public void setRoom(Room r) {									this.room=r;						}
	public void setContent(ArrayList<ItemOnTable> itemsOnTable) {	this.content=itemsOnTable;			}

	/**
	 * Returns the created item on table
	 * */
	public ItemOnTable addItem(Item i, double q) throws TableIsNotOpenException
	{
		if (!this.isOpen())
			throw new TableIsNotOpenException();
		
		ItemOnTable iot = new ItemOnTable(i,q,i.getPrice()*q);
		this.content.add(iot);
		i.notifyItemAdded(iot);
		this.registerItemAdded(iot);
		
		return iot;
	}

	public void removeItem(Item i, double q) throws TableIsNotOpenException, CantSubstractThatQuantityException {
		if (!this.isOpen())
			throw new TableIsNotOpenException();
		
		if (this.itemQuantity(i)<q)
			throw new CantSubstractThatQuantityException();
		
		ItemOnTable iot = new ItemOnTable(i, -1 *q,i.getPrice()*q);
		this.content.add(iot);
		this.registerItemRemoved(iot);
	}
	
	private Double itemQuantity(Item i) {
		Double q = new Double(0.0);
		for (ItemOnTable iot : this.content)
			if (iot.getItem().equals(i))
				q+=iot.getQuantity();
		return q;
	}

	private void registerItemAdded(ItemOnTable iot) {
		this.actionsLog.add(new AddItemToTableActionLog(this, iot));
	}

	private void registerItemRemoved(ItemOnTable iot)
	{
		this.actionsLog.add(new RemoveItemFromTableActionLog(this,iot));
	}

	public void changeRoom(Room r)
	{
		this.room.removeTable(this);
		this.room=r;
	}
	
	public void open() throws CouldNotOpenTableException {
		this.setState(this.state.openTable());
	}
	
	public void charge() throws CouldNotChargeException {
		this.setState(this.state.chargeTable());
	}
	
	public boolean isOpen() {
		return this.state.isOpen();
	}


	public void close(PayMethod payMethod) throws TableIsNotChargingException {
		Commerce.getInstance().getCurrentCash().registerSell(this.getAmount(), payMethod);
		this.setState(this.state.closeTable());
	}


	public Double getAmount() {
		Double amount=0.0;
		for (ItemOnTable i : this.content)
			amount+=i.getAmount();
		return amount;
	}

	public String getHistoryString() {
		StringBuilder sb = new StringBuilder("\n\n----"+this.name+" history: ");
		for (ActionLog al : this.actionsLog)
			sb.append("\n"+al.getDescription()+" - "+al.getDetail());
		
		sb.append("\n----");
		return sb.toString();
	}
	public String getId() { //temporary
		return this.name;
	}
	
}
