/**
 * This class enables the construction of a decision tree
 * 
 * @author Mehrdad Sabetzadeh, University of Ottawa
 * @author Guy-Vincent Jourdan, University of Ottawa
 *
 */

public class DecisionTree {

	private static class Node<E> {
		E data;
		Node<E>[] children;

		Node(E data) {
			this.data = data;
		}
	}

	Node<VirtualDataSet> root;

	/**
	 * @param data is the training set (instance of ActualDataSet) over which a
	 *             decision tree is to be built
	 */
	public DecisionTree(ActualDataSet data) {
		root = new Node<VirtualDataSet>(data.toVirtual());
		build(root);
	}

	/**
	 * The recursive tree building function
	 * 
	 * @param node is the tree node for which a (sub)tree is to be built
	 */
	@SuppressWarnings("unchecked")
	private void build(Node<VirtualDataSet> node) {
		// WRITE YOUR CODE HERE!
		boolean splitAgain = false;
		String a_maxName;
		Attribute a_max;
		VirtualDataSet[] partitions;
		if (node == null || node.data == null){
			throw new NullPointerException("Your node or data is null");
		}
		if(node.data.getNumberOfAttributes() < 1 || node.data.getNumberOfDatapoints() < 1){
			throw new IllegalArgumentException("There isnt any attributes or datapoints in this dataset");
		}

		if(node.data.getAttribute(node.data.getNumberOfAttributes()-1).getValues().length == 1){
			partitions = node.data.partitionByNominallAttribute(node.data.getNumberOfAttributes()-1);
			node.children = new Node[partitions.length];
			for(int i = 0; i < partitions.length; i++){
				node.children[i] = new Node(partitions[i]);
			}
			splitAgain = false;
		}
		if(node.data.getNumberOfAttributes() == 1){
			node.children = new Node[2];
			partitions = node.data.partitionByNominallAttribute(0);
			node.children[0] = new Node(partitions[0]);
			node.children[1] = new Node(partitions[1]);
			splitAgain = false;
		} 
		GainInfoItem[] gains = InformationGainCalculator.calculateAndSortInformationGains(node.data);
		for(int i = 0; i < gains.length; i++){
			if(gains[i].getGainValue() != 0){
				splitAgain = true;
			}
		}
		if(splitAgain == true){
			a_maxName = gains[0].getAttributeName();
			a_max = node.data.getAttribute(a_maxName);
			VirtualDataSet[] newPartitions;
			Node<VirtualDataSet>[] childrenVals;
			if(a_max.getType() == AttributeType.NOMINAL){
				newPartitions = node.data.partitionByNominallAttribute(node.data.getAttributeIndex(a_maxName));
				childrenVals = new Node[newPartitions.length];
				for(int i = 0; i < newPartitions.length; i++){
					childrenVals[i] = new Node<VirtualDataSet>(newPartitions[i]);
				}
				node.children = childrenVals;
			} else {
				String[] values = node.data.getAttribute(gains[0].getAttributeName()).getValues();

				int index = -1;

				for (int i = 0; i < values.length; i++) {
					if (values[i].equals(gains[0].getSplitAt())) {
						index = i;
						break;
					}
				}

				if (index == -1) {
					System.out.println("Houston, we have a problem!");
					return;
				}
				newPartitions = node.data.partitionByNumericAttribute(node.data.getAttributeIndex(a_maxName), index);
				childrenVals = new Node[newPartitions.length];
				for(int i = 0; i < newPartitions.length; i++){
					childrenVals[i] = new Node<VirtualDataSet>(newPartitions[i]);
				}
				node.children = childrenVals;
			}
			for(int i = 0; i < node.children.length; i++){
				build(node.children[i]);
			}
		}

	}

	@Override
	public String toString() {
		return toString(root, 0);
	}

	/**
	 * The recursive toString function
	 * 
	 * @param node        is the tree node for which an if-else representation is to
	 *                    be derived
	 * @param indentDepth is the number of indenting spaces to be added to the
	 *                    representation
	 * @return an if-else representation of node
	 */
	private String toString(Node<VirtualDataSet> node, int indentDepth) {
		// WRITE YOUR CODE HERE!
		StringBuffer buffer = new StringBuffer("");
		String name;
		String val;
		String[] vals;
		if(node.children[0].children == null){
			name = node.data.getAttribute(node.data.getNumberOfAttributes()-1).getName();
			vals = node.data.getAttribute(node.data.getNumberOfAttributes()-1).getValues();
			val = vals[0];
			buffer.append(createIndent(indentDepth) + name + " = " + val);
			return buffer.toString();

		}
		for(int i = 0; i < node.children.length; i++){
			if(i == 0 ){
				buffer.append(createIndent(indentDepth)+ "if (" + node.children[0].data.getCondition() + ") {\n" + toString(node.children[0], indentDepth+2) + "\n" + createIndent(indentDepth) + "}");
			} else{
				buffer.append("\n"+createIndent(indentDepth)+ "else if (" + node.children[i].data.getCondition() + ") {\n" + toString(node.children[i], indentDepth+2)+ "\n" + createIndent(indentDepth) + "}");
			}
		}
		return buffer.toString();
		// Remove the following line once you have implemented the method
	}

	/**
	 * @param indentDepth is the depth of the indentation
	 * @return a string containing indentDepth spaces; the returned string (composed
	 *         of only spaces) will be used as a prefix by the recursive toString
	 *         method
	 */
	private static String createIndent(int indentDepth) {
		if (indentDepth <= 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < indentDepth; i++) {
			buffer.append(' ');
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws Exception {
	
		StudentInfo.display();

		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("Expected a file name as argument! Usage: java DecisionTree <file name>");
		}

		String strFilename = args[0];

		ActualDataSet data = new ActualDataSet(new CSVReader(strFilename));

		DecisionTree dtree = new DecisionTree(data);

		System.out.println(dtree);
	}
}