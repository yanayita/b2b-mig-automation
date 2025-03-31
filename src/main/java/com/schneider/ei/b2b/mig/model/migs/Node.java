
package com.schneider.ei.b2b.mig.model.migs;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @JsonProperty("IsSelected")
    private Boolean isSelected;
    @JsonProperty("IsOriginalNode")
    private Boolean isOriginalNode;
    @JsonProperty("CodelistReferences")
    private List<Object> codelistReferences = new ArrayList<Object>();
    @JsonProperty("CodelistReferenceDeselected")
    private Boolean codelistReferenceDeselected;
    @JsonProperty("OverrideSimpleTypeCodelistReferences")
    private Boolean overrideSimpleTypeCodelistReferences;
    @JsonProperty("NodeStatus")
    private NodeStatus nodeStatus;
    @JsonProperty("VertexGUID")
    private String vertexGUID;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("XMLNodeName")
    private String xMLNodeName;
    @JsonProperty("NodeCategory")
    private String nodeCategory;
    @JsonProperty("Documentation")
    private Value documentation;
    @JsonProperty("Domain")
    private Domain domain;
    @JsonProperty("ComplexTypeVertexGUID")
    private String complexTypeVertexGUID;
    @JsonProperty("BaseTypeDomain")
    private Domain baseTypeDomain;
    @JsonProperty("Qualifiers")
    private List<Qualifier> qualifiers = new ArrayList<>();
    @JsonProperty("QualifierMarkers")
    private List<QualifierMarker> qualifierMarkers = new ArrayList<QualifierMarker>();
    @JsonProperty("NumberOfNodes")
    private Integer numberOfNodes;
    @JsonProperty("Nodes")
    private List<Node> nodes = new ArrayList<>();
    @JsonProperty("Properties")
    private Properties properties;
    @JsonProperty("PropertySets")
    private PropertySets propertySets;
    @JsonProperty("IDProperties")
    private IDProperties iDProperties;
    @JsonProperty("SimpleTypeVertexGUID")
    private String simpleTypeVertexGUID;
    @JsonProperty("SelectedCodelist")
    private SelectedCodelist selectedCodelist;
    @JsonProperty("BaseTypeFacetProperties")
    private BaseTypeFacetProperties baseTypeFacetProperties;
    @JsonProperty("Namespace")
    private String namespace;
    @JsonProperty("Form")
    private String form;
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Node parent;

}
