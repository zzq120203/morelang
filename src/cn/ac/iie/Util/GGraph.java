package cn.ac.iie.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.ac.iie.Util.GNode.GNodeJ;

public class GGraph {
	private final static Logger LOG = LoggerFactory.getLogger(GGraph.class);
	
	public Set<GCategory> graph_categories;
	public Set<GNode> graph_nodes;
	public Set<GLink> graph_links;
	public List<String> colorList;
	public int colorIndex = 0;
	
	public GGraph() {
		graph_categories = new HashSet<GCategory>();
		graph_nodes = new HashSet<GNode>();
		graph_links = new HashSet<GLink>();
		colorList = new ArrayList<String>();
	}
	
	public boolean isInCategories(String category) {
		boolean r = false;
		
		for (GCategory cg : graph_categories) {
			if (cg.name.equalsIgnoreCase(category)) {
				r = true;
				break;
			}
		}
		
		return r;
	}

	public boolean isInLinks(String src, String dst) {
		boolean r = false;
		
		for (GLink l : graph_links) {
			if (l.source.equalsIgnoreCase(src) && l.target.equalsIgnoreCase(dst)) {
				r = true;
				break;
			}
		}
		
		return r;
	}
	
	public void addNode(String category, String _id, int nType) {
		/*
		 * check category
		 */
		if (!isInCategories(category)) {
			GCategory cg = new GCategory(category);
			graph_categories.add(cg);
			colorIndex++;
		}
		boolean isOldNode = false;

		for (GNode n : graph_nodes) {
			if (n.j.name.equals(_id)) {
				// increase value
				if (!n.category.equals(category)) {
					// update categories
					String ncg = n.updateCategory(category);
					if (ncg != null) {
						GCategory cg = new GCategory(ncg);
						graph_categories.add(cg);
						colorIndex++;
					}
				}
				isOldNode = true;
				break;
			}
		}
		if (!isOldNode) {
			GNode n = new GNode(category, _id, nType);
			graph_nodes.add(n);
		}
	}
	
	public void addLink(String source, String target, boolean aggr) {
		boolean isFound = false;
		
		for (GLink l : graph_links) {
			if (l.source.equalsIgnoreCase(source) && l.target.equalsIgnoreCase("target")) {
				if (aggr) {
					l.value++;
				}
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			GLink l = new GLink(source, target);
			graph_links.add(l);	
		}
		for (GNode n : graph_nodes) {
			if (n.j.name.equals(source)) {
				n.j.value++;
			}
			if (n.j.name.equals(target)) {
				n.j.value++;
			}
		}
	}
	
	public void genColorList(int colorNr) {
		int step = 0xFFFFFF / colorNr;
		
		for (int i = 0; i < 0xFFFFFF; i += step) {
			colorList.add(String.format("#%x%x%x", (i & 0xFF), (i & 0xFF00) >> 8, (i & 0xFF0000) >> 16));
		}
		int i = 0;
		for (GCategory gc : graph_categories) {
			gc.setColor(colorList.get(i));
			i++;
		}
	}
	
	public GGraph subGraphKeepSignificant(GGraph origin, int nType, int weight) {
		GGraph subG = new GGraph();
		HashSet<String> removedNodes = new HashSet<String>();
		HashSet<GNode> tmp_graph_nodes = new HashSet<GNode>();
		
		subG.graph_categories = origin.graph_categories;
		for (GNode n : origin.graph_nodes) {
			if (n.nType == nType) {
				if (n.j.value >= weight) {
					tmp_graph_nodes.add(n);
				} else {
					removedNodes.add(n.j.name);
				}
			} else {
				tmp_graph_nodes.add(n);
			}
		}
		for (GLink l : origin.graph_links) {
			if (removedNodes.contains(l.source) || removedNodes.contains(l.target)) {
				// removed this link
				for (GNode n : tmp_graph_nodes) {
					if (n.j.name.equals(l.source) || n.j.name.equals(l.target)) {
						n.j.value--;
					}
				}
			} else {
				subG.graph_links.add(l);
			}
		}
		for (GNode n : tmp_graph_nodes) {
			if (n.j.value > 0)
				subG.graph_nodes.add(n);
		}
		subG.colorIndex = origin.colorIndex;
		LOG.info("   G's node size = " + origin.graph_nodes.size() + ", link size = " + origin.graph_links.size());
		LOG.info("subG's node size = " + subG.graph_nodes.size() + ", link size = " + subG.graph_links.size());
		
		return subG;
	}
	
	public GGraph subGraphRemoveUnconnected(GGraph origin) {
		GGraph subG = new GGraph();
		// remove not connected groups and users
		HashSet<String> directTarget = new HashSet<String>();
		HashSet<String> directSource = new HashSet<String>();
		HashSet<String> removedNodes = new HashSet<String>();
		
		for (GNode n : origin.graph_nodes) {
			boolean shouldRemove = true;
			
			// for each node, detect its direct source and target
			directTarget.clear();
			directSource.clear();
			for (GLink l : origin.graph_links) {
				if (l.source.equals(n.j.name)) {
					// add to direct target group
					directTarget.add(l.target);
				}
				if (l.target.equals(n.j.name)) {
					// add to direct source group
					directSource.add(l.source);
				}
			}
			
			// test if this node connected to other nodes in same type
			for (GLink l : origin.graph_links) {
				if (directTarget.contains(l.source)) {
					if (!l.target.equals(n.j.name)) {
						shouldRemove = false;
						break;
					}
				}
				if (directTarget.contains(l.target)) {
					if (!l.source.equals(n.j.name)) {
						shouldRemove = false;
						break;
					}
				}
				if (directSource.contains(l.target)) {
					if (!l.source.equals(n.j.name)) {
						shouldRemove = false;
						break;
					}
				}
				if (directSource.contains(l.source)) {
					if (!l.target.equals(n.j.name)) {
						shouldRemove = false;
						break;
					}
				}
			}
			if (shouldRemove) {
				removedNodes.add(n.j.name);
			}
		}
		LOG.info("removedNodes' size = " + removedNodes.size());
		
		subG.graph_categories = origin.graph_categories;
		for (GNode n : origin.graph_nodes) {
			if (!removedNodes.contains(n.j.name)) {
				subG.graph_nodes.add(n);
			}
		}
		for (GLink l : origin.graph_links) {
			if (removedNodes.contains(l.source) || removedNodes.contains(l.target)) {
				// removed this link
			} else {
				subG.graph_links.add(l);
			}
		}
		subG.colorIndex = origin.colorIndex;
		LOG.info("   G's node size = " + origin.graph_nodes.size() + ", link size = " + origin.graph_links.size());
		LOG.info("subG's node size = " + subG.graph_nodes.size() + ", link size = " + subG.graph_links.size());
		
		return subG;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		genColorList(colorIndex);
		
		List<GCategory> gcategories = new ArrayList<GCategory>();
		
		gcategories.addAll(graph_categories);
		
		sb.append("{\"categories\":");
		sb.append(JSON.toJSON(gcategories));
		sb.append(",");
		
		sb.append("\"color\":");
		sb.append(JSON.toJSON(colorList));
		sb.append(",");
		
		List<GNodeJ> gnodej = new ArrayList<GNodeJ>();
		for (GNode n : graph_nodes) {
			for (int i = 0; i < gcategories.size(); i++) {
				if (gcategories.get(i).name.equalsIgnoreCase(n.category)) {
					n.j.category = i;
					break;
				}
			}
			n.j.value *= 1;
			gnodej.add(n.j);
		}
		sb.append("\"nodes\":");
		sb.append(JSON.toJSON(gnodej));
		sb.append(",");
		
		List<GLink> glink = new ArrayList<GLink>();
		glink.addAll(graph_links);
		sb.append("\"links\":");
		sb.append(JSON.toJSON(glink));
		sb.append("}");
		
		return sb.toString();
	}
}
