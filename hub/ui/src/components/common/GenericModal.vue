<template>
  <div class="modal fade" tabindex="-1" ref="genericModal" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h1 class="modal-title fs-5">
            {{ title }}
          </h1>
          <button
            type="button"
            class="btn-close"
            @click="modal.hide()"
            aria-label="Close"
          ></button>
        </div>
        <div class="modal-body">
          <slot name="content"></slot>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="modal.hide()">
            Cancel
          </button>
          <slot name="buttonSlot"></slot>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { Modal } from "bootstrap";
export default {
  name: "genericModal",
  props: ["title"],
  data() {
    return { modal: null };
  },
  watch: {},
  methods: {
    displayModal() {
      this.modal.show();
    },
    hideModal() {
      this.modal.hide();
    },
  },
  mounted: function () {
    this.modal = new Modal(this.$refs.genericModal);
  },
};
</script>
