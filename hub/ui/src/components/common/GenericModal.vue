<template>
  <div
    class="modal fade"
    tabindex="-1"
    ref="genericModal"
    aria-hidden="true"
    v-on="{
      'shown.bs.modal': handleShownModal,
      'hidden.bs.modal': handleHiddenModal,
    }"
  >
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h1 class="modal-title fs-5">
            {{ title }}
          </h1>
          <slot name="closeSlot">
            <button
              type="button"
              class="btn-close"
              @click="modal.hide()"
              aria-label="Close"
            ></button>
          </slot>
        </div>
        <div class="modal-body">
          <slot name="content"></slot>
        </div>
        <div class="modal-footer">
          <slot name="cancelSlot"
            ><button
              type="button"
              class="btn btn-secondary"
              @click="modal.hide()"
            >
              Cancel
            </button></slot
          >
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
    return { modal: null, shown: false };
  },
  watch: {},
  methods: {
    handleShownModal() {
      this.shown = true;
    },
    handleHiddenModal() {
      this.shown = false;
    },
    isModalShown() {
      return this.shown;
    },
    displayModal() {
      this.modal.show();
      // wait for modal to show
      return new Promise((resolve) => this.waitForModalShown(resolve));
    },
    waitForModalShown(resolve) {
      if (!this.shown) {
        this.sleep(50).then(() => {
          this.waitForModalShown(resolve);
        });
      } else {
        resolve();
      }
    },
    sleep(ms) {
      return new Promise((resolve) => setTimeout(resolve, ms));
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
