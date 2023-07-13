package com.cnpc.epai.core.workscene.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    private Map<String, Object> attachment;
    private List<Object> inputResource;
    private List<Object> outputResource;
    private List<Node> child;

    public Node() {
        attachment = new HashMap<>();
        inputResource = new ArrayList<>();
        outputResource = new ArrayList<>();
        child = new ArrayList<>();
    }

    public Node(Map<String, Object> attach, List<Object> inResource, List<Object> outResource) {
        this();
        attachment.putAll(attach);
        if (inResource != null)
            inputResource.addAll(inResource);
        if (outResource != null)
            outputResource.addAll(outResource);
    }

    public List<Object> getInputResource() {
        return inputResource;
    }

    public List<Object> getOutputResource() {
        return outputResource;
    }

    public void addChild(Node node) {
        child.add(node);
    }

    public List<Node> getChild() {
        return child;
    }

    public void addAttachment(String key, Object value) {
        attachment.put(key, value);
    }

    public void addAttachment(Map<String, Object> value) {
        attachment.putAll(value);
    }

    public Object getAttachment(String key) {
        return attachment.get(key);
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }
}
