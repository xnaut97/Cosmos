package com.github.xnaut97.cosmos.menu.state;

import lombok.Getter;

@Getter
public class PagedMenuState extends MenuState {

    private int currentPage;
    private int itemsPerPage;
    private int startSlot;
    private int endSlot;
    private int spacing;

    public PagedMenuState currentPage(int currentPage) {
        this.currentPage = Math.max(0, currentPage);
        return this;
    }

    public PagedMenuState itemsPerPage(int itemsPerPage) {
        this.itemsPerPage = Math.max(0, itemsPerPage);
        return this;
    }

    public PagedMenuState startSlot(int startSlot) {
        this.startSlot = startSlot;
        return this;
    }

    public PagedMenuState endSlot(int endSlot) {
        this.endSlot = endSlot;
        return this;
    }

    public PagedMenuState spacing(int spacing) {
        this.spacing = Math.max(0, spacing);
        return this;
    }
}
